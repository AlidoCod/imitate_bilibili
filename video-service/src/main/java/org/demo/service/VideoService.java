package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RabbitMQConstant;
import org.demo.constant.CacheEntry;
import org.demo.constant.EntityConstant;
import org.demo.dto.VideoUpdateDto;
import org.demo.dto.file.VideoMergeParamDto;
import org.demo.mapper.ImageMapper;
import org.demo.mapper.SeriesMapper;
import org.demo.mapper.VideoMapper;
import org.demo.pojo.GlobalRuntimeException;
import org.demo.pojo.Video;
import org.demo.util.ContentTypeUtil;
import org.demo.util.ObjectConverter;
import org.demo.util.ThreadHolder;
import org.demo.vo.Result;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.demo.constant.EntityConstant.CHUNK_SUFFIX;


@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final MinioService minioService;
    private final ImageMapper imageMapper;
    private final SeriesMapper seriesMapper;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RabbitTemplate rabbitTemplate;

    public Result<Void> checkVideo(String md5) {
        Video video = videoMapper.selectOne(new QueryWrapper<Video>().eq("md5", md5));
        return video.getId() == null ? Result.success(EntityConstant.YES) : Result.success(EntityConstant.NO + ", videoId: " + video.getId());
    }

    public Result<Void> checkVideoChunk(String md5, int index) {
        String chunkFilePath = getChunkFileFolderPath(md5) + index;
        try {
            InputStream inputStream = minioService.getDownloadInputStream(EntityConstant.VIDEO_BUCKET, chunkFilePath);
            if (inputStream != null)
                return Result.success(EntityConstant.NO + ", chunk index: " + index);
            else
                return Result.success(EntityConstant.YES);
        } catch (Exception e) {
            return Result.success(EntityConstant.YES);
        }
    }

    public Result<Void> uploadVideoChunk(MultipartFile chunk, String md5, int index) {
        String chunkFilePath = getChunkFileFolderPath(md5) + index;
        try {
            minioService.upload(EntityConstant.IMAGE_BUCKET, chunk.getInputStream(), chunkFilePath, null);
            return Result.success();
        } catch (Exception e) {
            throw GlobalRuntimeException.of("分块上传失败", e);
        }
    }

    @Transactional
    public Result<Void> mergeVideoChunk(VideoMergeParamDto dto) {
        Video video = new Video();
        Long seriesId = dto.getSeriesId();
        // 验证系列ID，确认是本人创建且系列ID存在
        if (seriesId != null && seriesMapper.selectByMap(Map.of("id", seriesId, "user_id", ThreadHolder.getUser().getId())) != null)
            video.setSeriesId(seriesId);
        // 验证封面是否存在
        if (imageMapper.selectById(dto.getImageId()) == null)
            return Result.fail("封面不存在，视频分块已上传成功，请重新上传封面并请求");
        // 属性设置
        String md5 = dto.getMd5();
        String path = getVideoFileFolderPath(md5);
        // 文件名字 = /xxx/ + md5 + ".xxx"
        String name = md5 + dto.getSuffix();
        video.setVideoPath(path + name);
        video.setImageId(dto.getImageId());
        video.setVideoSuffix(dto.getSuffix());
        video.setMd5(dto.getMd5());
        video.setDescription(dto.getDescription());
        video.setTitle(dto.getTitle());
        try {
            minioService.merge(path, name, dto.getMaxIndex());
            for (int i = 0; i <= dto.getMaxIndex(); i++) {
                int temp = i;
                threadPoolTaskExecutor.execute(() -> {
                    try {
                        minioService.remove(EntityConstant.VIDEO_BUCKET, path + CHUNK_SUFFIX + temp);
                    } catch (Exception e) {
                        // 若删除失败，向mq发送消息，保留数据，保留日志
                        rabbitTemplate.convertAndSend(RabbitMQConstant.DEMO_EXCHANGE, RabbitMQConstant.ASYNC_ROUTING_KEY, "error: minio remove fail, videoId: " + video.getId());
                    }
                });
            }
            videoMapper.insert(video);
            return Result.success();
        }catch (Exception e) {
            throw GlobalRuntimeException.of("文件合并失败", e);
        }
    }

    public Result<Void> play(Long videoId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception{
        Video video = videoMapper.selectOne(new QueryWrapper<Video>().eq("id", videoId));
        if (httpServletRequest.getHeader("Range") == null)
            return Result.fail("不存在的Range请求头");
        if (!video.getVideoSuffix().equals(".mp4"))
            return Result.fail("抱歉，此视频非mp4格式，不可直接播放，可下载后播放");
        BufferedInputStream inputStream = new BufferedInputStream(minioService.getDownloadInputStream(EntityConstant.VIDEO_BUCKET, video.getVideoPath()), 8 * EntityConstant.KB);
        // 修改默认缓冲区为http3最大值8KB
        Long size = video.getSize();

        CacheEntry<Long, Long> range = analyzeRange(httpServletRequest.getHeader("Range"), size);
        if (range == null)
            return Result.fail("错误的Range请求头，请检查请求头设置，格式为startIndex - endIndex");
        /*
         * 设置Http响应头
         * */
        httpServletResponse.setContentType(ContentTypeUtil.getContentTypeBySuffix(video.getVideoSuffix()));
        httpServletResponse.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(size));
        httpServletResponse.setHeader(HttpHeaders.CONTENT_RANGE, range.key + "-" + range.value);
        httpServletResponse.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, "video/mp4");
        httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        OutputStream os = httpServletResponse.getOutputStream();
        long len = range.value - range.key;
        long temp;
        if ((temp = inputStream.skip(range.key)) != range.key) {
            log.warn("跳过的字节数错误: {}, 实际需要跳过的字节数: {}", temp, range.key);
        }
        byte[] buf = new byte[len < 8 * EntityConstant.KB ? (int) len : 8 * EntityConstant.KB];
        //http3最大只能一次8KB，放弃吧
        int limit;
        try {
            while (len > 0 && (limit = inputStream.read(buf)) != -1) {
                os.write(buf, 0, (int) Math.min(limit, len));
                //如果读取到字节数大于len，那么就只读取len，避免传输过多的字节数
                len -= limit;
                //仍然需要读取的字节数 = 上一次仍然需要读取的字节数 - 这次读取的字节数
                log.debug("len: {}", len);
            }
        } catch (Exception e) {
            log.warn("", e);
        } finally {
            os.close();
            inputStream.close();
        }
        return Result.success();
    }

    public Result<Void> update(VideoUpdateDto dto) throws JsonProcessingException {
        Video video = ObjectConverter.convert(dto, Video.class);
        return videoMapper.updateById(video) == 1 ? Result.success(EntityConstant.YES) : Result.fail(EntityConstant.NO);
    }

    /**
     *  注意不能删除文件系统的文件
     *  理论上，文件系统的文件和数据库的数据是一对一的关系
     */
    @Transactional
    public Result<Void> delete(Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null)
            return Result.success("视频ID不存在");
        if (!(videoMapper.deleteById(id) == 1))
            return Result.fail("数据库删除失败，发生异常");
        try {
            minioService.remove(EntityConstant.VIDEO_BUCKET, video.getVideoPath());
        } catch (Exception e) {
            log.warn("", e);
            return Result.success("文件系统文件删除失败, 请通知管理员查看异常");
        }
        return Result.yes();
    }

    /**
     * 解析range，解析出起始byte（start）和结束byte（end)
     */
    private CacheEntry<Long, Long> analyzeRange(String range, Long size) {
        range = range.replace(" ", "");
        //去除所有空格
        String[] split = range.split("-");
        //分割字符串
        if (split.length != 2)
            return null;
        Long start = Long.parseLong(split[0]);
        Long end = Long.parseLong(split[1]);
        //说明是未知range，格式错误
        return new CacheEntry<>(start, end < size ? end : size);
    }

    private String getVideoFileFolderPath(String fileMd5) {
        return "/" + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/";
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return "/" + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + CHUNK_SUFFIX;
    }
}
