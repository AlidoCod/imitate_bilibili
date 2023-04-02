package org.demo.core.service;

import jakarta.servlet.http.HttpServletResponse;
import org.demo.core.controller.dto.file.VideoMergeParamDto;
import org.demo.core.controller.dto.file.MultipartFileParamDto;
import org.demo.core.util.JsonBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    JsonBean<String> uploadImage(MultipartFile multipartFile, MultipartFileParamDto dto) throws IOException;

    JsonBean<Void> checkImage(String md5);

    JsonBean<Void> checkVideo(String md5);

    JsonBean<Void> checkVideoChunk(String md5, int index);

    JsonBean<Void> uploadVideoChunk(MultipartFile chunk, String md5, int index);

    JsonBean<Void> downloadImage(Long imageId, HttpServletResponse response);

    JsonBean<Void> mergeVideoChunk(MultipartFile chunk, VideoMergeParamDto dto);
}
