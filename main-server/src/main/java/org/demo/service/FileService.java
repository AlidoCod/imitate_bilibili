package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.demo.constant.EntityConstant;
import org.demo.controller.dto.file.MultipartFileParamDto;
import org.demo.controller.dto.file.VideoMergeParamDto;
import org.demo.core.dao.ImageMapper;
import org.demo.core.dao.SeriesMapper;
import org.demo.core.dao.VideoMapper;
import org.demo.core.pojo.Image;
import org.demo.core.pojo.Video;
import org.demo.exception.GlobalRuntimeException;
import org.demo.util.ContentTypeUtil;
import org.demo.controller.vo.JsonBean;
import org.demo.util.ThreadHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    public JsonBean<String> uploadImage(MultipartFile multipartFile, MultipartFileParamDto dto) {
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
                return JsonBean.successByData(String.valueOf(one.getId()));
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
        return JsonBean.successByData(String.valueOf(image.getId()));
    }

    public JsonBean<Void> checkImage(String md5) {
        Image image = new Image();
        image.setMd5(md5);
        image = imageMapper.selectOne(new QueryWrapper<Image>().eq("md5", md5));
        if (image.getId() == null)
            return JsonBean.fail("未找到对应的图片文件");
        try {
            if (minioService.getDownloadInputStream(EntityConstant.IMAGE_BUCKET, image.getImagePath()) != null) {
                return JsonBean.success(String.valueOf(image.getId()));
            } else {
                imageMapper.deleteById(image.getId());
                return JsonBean.fail("未找到对应的图片文件");
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | IOException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw GlobalRuntimeException.of("未找到对应的图片文件", e);
        }
    }

    public JsonBean<Void> checkVideo(String md5) {
        Video video = new Video();
        video.setMd5(md5);
        video = videoMapper.selectOne(new QueryWrapper<Video>().eq("md5", md5));
        if (video.getId() == null)
            return JsonBean.fail("未找到对应的图片文件");
        try {
            if (minioService.getDownloadInputStream(EntityConstant.IMAGE_BUCKET, video.getVideoPath()) != null) {
                return JsonBean.success(String.valueOf(video.getId()));
            } else {
                imageMapper.deleteById(video.getId());
                return JsonBean.fail("未找到对应的图片文件");
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | IOException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw GlobalRuntimeException.of("未找到对应的图片文件", e);
        }
    }

    public JsonBean<Void> checkVideoChunk(String md5, int index) {
        String chunkFilePath = getChunkFileFolderPath(md5) + index;
        try {
            InputStream inputStream = minioService.getDownloadInputStream(EntityConstant.VIDEO_BUCKET, chunkFilePath);
            if (inputStream != null)
                return JsonBean.success(String.valueOf(index));
            else
                return JsonBean.fail("未找到文件分块");
        } catch (Exception e) {
            return JsonBean.fail("未找到文件分块");
        }
    }

    public JsonBean<Void> uploadVideoChunk(MultipartFile chunk, String md5, int index) {
        String chunkFilePath = getChunkFileFolderPath(md5) + index;
        try {
            minioService.upload(EntityConstant.IMAGE_BUCKET, chunk.getInputStream(), chunkFilePath, null);
            return JsonBean.success();
        } catch (Exception e) {
            throw GlobalRuntimeException.of("分块上传失败", e);
        }
    }

    public JsonBean<Void> downloadImage(Long imageId, HttpServletResponse response) {
        Image image = imageMapper.selectById(imageId);
        if (image == null)
            return JsonBean.fail("图片id不存在");
        try {
            minioService.download(EntityConstant.IMAGE_BUCKET, image.getImagePath(), response.getOutputStream());
            return JsonBean.success();
        } catch (Exception e) {
            throw GlobalRuntimeException.of("图片ID不存在", e);
        }
    }

    public JsonBean<Void> mergeVideoChunk(MultipartFile chunk, VideoMergeParamDto dto) {
        Video video = new Video();
        Long seriesId = dto.getSeriesId();
        // 验证系列ID，确认是本人创建且系列ID存在
        if (seriesId != null && seriesMapper.selectByMap(Map.of("id", seriesId, "user_id", ThreadHolder.getUser().getId())) != null)
            video.setSeriesId(seriesId);
        // 验证封面是否存在
        if (imageMapper.selectById(dto.getImageId()) == null)
            return JsonBean.fail("封面不存在，视频已上传成功，请重新上传封面并请求");
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
            return JsonBean.success();
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

    @Data
    @AllArgsConstructor
    static class CacheEntry<K, V> {

        K key;
        V value;
    }

}
