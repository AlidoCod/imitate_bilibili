package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.demo.constant.CacheEntry;
import org.demo.constant.EntityConstant;
import org.demo.dto.file.MultipartFileParamDto;
import org.demo.mapper.ImageMapper;
import org.demo.pojo.base.GlobalRuntimeException;
import org.demo.pojo.Image;
import org.demo.util.ContentTypeUtil;
import org.demo.vo.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    // 路径缓存
    private CacheEntry<LocalDate, String> entry;

    private final MinioService minioService;

    private final ImageMapper imageMapper;

    @Transactional
    public Result<String> uploadImage(MultipartFile multipartFile, MultipartFileParamDto dto) {
        // 远程请求Json解析字符串时，有可能会加上换行符，不一定
        dto.setMd5(dto.getMd5().substring(0, 32));
        Image image = new Image();
        try {
            // MD5校验
            if (!verifyMd5(multipartFile, dto.getMd5())) {
                throw GlobalRuntimeException.of("MD5校验失败，文件损坏");
            }

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
        if (image.getId() == null) {
            return Result.fail("未找到对应的图片文件");
        }
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



    public Result<Void> downloadImage(Long imageId, HttpServletResponse response) {
        Image image = imageMapper.selectById(imageId);
        if (image == null) {
            return Result.fail("图片id不存在");
        }
        try {
            minioService.download(EntityConstant.IMAGE_BUCKET, image.getImagePath(), response.getOutputStream());
            return Result.success();
        } catch (Exception e) {
            throw GlobalRuntimeException.of("图片ID不存在", e);
        }
    }

    private boolean verifyMd5(MultipartFile multipartFile, String md5) throws IOException {
        String digestAsHex = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
        return digestAsHex.equals(md5);
    }


        private String getFileFolder() {
            LocalDate now = LocalDate.now();
            // 如果缓存存在，则直接返回
            if (entry != null && entry.getKey().equals(now)) {
                return entry.getValue();
            }
            String s = "/" + now.toString().replace("-", "/") + "/";
            entry = new CacheEntry<>(now, s);
            return entry.getValue();
        }

}
