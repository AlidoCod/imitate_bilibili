package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.demo.constant.EntityConstant;
import org.demo.dto.file.MultipartFileParamDto;
import org.demo.dto.file.VideoMergeParamDto;
import org.demo.mapper.ImageMapper;
import org.demo.mapper.SeriesMapper;
import org.demo.mapper.VideoMapper;
import org.demo.pojo.GlobalRuntimeException;
import org.demo.pojo.Image;
import org.demo.pojo.Video;
import org.demo.util.ContentTypeUtil;
import org.demo.util.ThreadHolder;
import org.demo.vo.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    // 路径缓存
    private CacheEntry<LocalDate, String> entry;

    private final MinioService minioService;

    private final ImageMapper imageMapper;

    private final VideoMapper videoMapper;

    private final SeriesMapper seriesMapper;

    @Transactional
    public Result<String> uploadImage(MultipartFile multipartFile, MultipartFileParamDto dto) {
        // 远程请求Json解析字符串时，有可能会加上换行符，不一定
        dto.setMd5(dto.getMd5().substring(0, 32));
        Image image = new Image();
        try {
            // MD5校验
            if (!verifyMd5(multipartFile, dto.getMd5()))
                throw GlobalRuntimeException.of("MD5校验失败，文件损坏");

            // MD5若一致，则秒传
            Image one = imageMapper.selectOne(new QueryWrapper<Image>().eq("md5", dto.getMd5()));
            if (one != null) {
                return Result.successByData(String.valueOf(one.getId()));
            }

            // 计算文件路径
            String fileName = dto.getMd5() + dto.getSuffix();
            String destPath = getFileFolder() + fileName;

            // 上传文件内容至数据库
            image.setImagePath(destPath);
            image.setMd5(dto.getMd5());
            imageMapper.insert(image);

            // 上传至文件系统
            minioService.upload(EntityConstant.IMAGE_BUCKET, multipartFile.getInputStream(), destPath, ContentTypeUtil.getContentType(dto.getSuffix()));

        } catch (TooManyResultsException e) {
            throw GlobalRuntimeException.of("md5值重复");
        } catch (Exception e) {
            throw GlobalRuntimeException.of("图片上传失败", e);
        }
        return Result.successByData(String.valueOf(image.getId()));
    }

    public Result<Void> checkImage(String md5) {
        Image image = new Image();
        image.setMd5(md5);
        image = imageMapper.selectOne(new QueryWrapper<Image>().eq("md5", md5));
        if (image.getId() == null)
            return Result.fail("未找到对应的图片文件");
        try {
            if (minioService.getDownloadInputStream(EntityConstant.IMAGE_BUCKET, image.getImagePath()) != null) {
                return Result.success(String.valueOf(image.getId()));
            } else {
                imageMapper.deleteById(image.getId());
                return Result.fail("未找到对应的图片文件");
            }
        } catch (InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | IOException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | io.minio.errors.ServerException | io.minio.errors.ErrorResponseException e) {
            throw GlobalRuntimeException.of("未找到对应的图片文件", e);
        }
    }

    public Result<Void> checkVideo(String md5) {
        Video video = new Video();
        video.setMd5(md5);
        video = videoMapper.selectOne(new QueryWrapper<Video>().eq("md5", md5));
        if (video.getId() == null)
            return Result.fail("未找到对应的图片文件");
        try {
            if (minioService.getDownloadInputStream(EntityConstant.IMAGE_BUCKET, video.getVideoPath()) != null) {
                return Result.success(String.valueOf(video.getId()));
            } else {
                imageMapper.deleteById(video.getId());
                return Result.fail("未找到对应的图片文件");
            }
        } catch (InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | IOException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | ServerException | io.minio.errors.ErrorResponseException e) {
            throw GlobalRuntimeException.of("未找到对应的图片文件", e);
        }
    }

    public Result<Void> checkVideoChunk(String md5, int index) {
        String chunkFilePath = getChunkFileFolderPath(md5) + index;
        try {
            InputStream inputStream = minioService.getDownloadInputStream(EntityConstant.VIDEO_BUCKET, chunkFilePath);
            if (inputStream != null)
                return Result.success(String.valueOf(index));
            else
                return Result.fail("未找到文件分块");
        } catch (Exception e) {
            return Result.fail("未找到文件分块");
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

    public Result<Void> downloadImage(Long imageId, HttpServletResponse response) {
        Image image = imageMapper.selectById(imageId);
        if (image == null)
            return Result.fail("图片id不存在");
        try {
            minioService.download(EntityConstant.IMAGE_BUCKET, image.getImagePath(), response.getOutputStream());
            return Result.success();
        } catch (Exception e) {
            throw GlobalRuntimeException.of("图片ID不存在", e);
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
            return Result.fail("封面不存在，视频已上传成功，请重新上传封面并请求");
        // 属性设置
        String md5 = dto.getMd5();
        video.setTags(dto.getTags());
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
            videoMapper.insert(video);
            return Result.success();
        }catch (Exception e) {
            throw GlobalRuntimeException.of("文件合并失败", e);
        }
    }

    public static final String CHUNK_SUFFIX = "chunk_";

    private String getVideoFileFolderPath(String fileMd5) {
        return "/" + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/";
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return "/" + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + CHUNK_SUFFIX;
    }

    private boolean verifyMd5(MultipartFile multipartFile, String md5) throws IOException {
        String digestAsHex = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
        return digestAsHex.equals(md5);
    }


    private String getFileFolder() {
        LocalDate now = LocalDate.now();
        // 如果缓存存在，则直接返回
        if (entry != null && entry.getKey().equals(now))
            return entry.getValue();
        String s = "/" + now.toString().replace("-", "/") + "/";
        entry = new CacheEntry<>(now, s);
        return entry.getValue();
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

    @Data
    @AllArgsConstructor
    static class CacheEntry<K, V> {

        K key;
        V value;
    }

}
