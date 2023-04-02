package org.demo.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.aop.annotation.EnableAutoLog;
import org.demo.core.controller.dto.file.VideoMergeParamDto;
import org.demo.core.controller.dto.file.MultipartFileParamDto;
import org.demo.core.service.FileService;
import org.demo.core.util.JsonBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 默认由服务器上传到文件系统的文件不会损坏，不进行MD5校验
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "文件信息")
@RequestMapping("/file")
@RestController
public class FileController {

    private final FileService fileService;

    @Operation(summary = "图片MD5校验", description = "图片上传前确保图片在数据库/文件系统不存在")
    @ApiResponse(responseCode = "200", description = "成功时，返回imageId")
    @EnableAutoLog
    @GetMapping("/image/check")
    public JsonBean<Void> checkImage(@RequestParam("md5") String md5) {
        return fileService.checkImage(md5);
    }

    @Operation(summary = "视频MD5校验", description = "视频上传前确保视频分块在数据库/文件系统不存在")
    @ApiResponse(responseCode = "200", description = "若成功，则视频已存在，请勿重复上传（不想写API了）")
    @EnableAutoLog
    @GetMapping("/video/check")
    public JsonBean<Void> checkVideo(@RequestParam("md5") String md5) {
        return fileService.checkVideo(md5);
    }

    @Operation(summary = "视频分块MD5校验", description = "视频上传前确保视频分块在文件系统不存在")
    @ApiResponse(responseCode = "200", description = "成功时，返回分块下标")
    @EnableAutoLog
    @GetMapping("/video/chunk/check")
    public JsonBean<Void> checkVideoChunk(@RequestParam("md5") String md5, @RequestParam("index") int index) {
        return fileService.checkVideoChunk(md5, index);
    }

    @Operation(summary = "视频分块上传", description = "校验通过的分块不必重新上传，节省带宽，必须校验所有分块，后端无法确认分块状态")
    @ApiResponse(responseCode = "200", description = "")
    @EnableAutoLog
    @PostMapping(value = "/video/chunk/upload", consumes = "multipart/form-data")
    public JsonBean<Void> uploadVideoChunk(@RequestPart("chunk") MultipartFile chunk, @RequestParam("md5") String md5, @RequestParam("index") int index) {
        return fileService.uploadVideoChunk(chunk, md5, index);
    }

    @Operation(summary = "视频分块合并", description = "请确保所有分块上传成功，上传失败的分块一定要重传")
    @ApiResponse(responseCode = "200", description = "")
    @EnableAutoLog
    @PostMapping(value = "/video/chunk/merge")
    public JsonBean<Void> mergeVideoChunk(@RequestPart("chunk") MultipartFile chunk, VideoMergeParamDto dto) {
        return fileService.mergeVideoChunk(chunk, dto);
    }

    @Operation(summary = "图片/封面上传", description = "")
    @EnableAutoLog
    @PostMapping(value = "/image/upload", consumes = "multipart/form-data")
    public JsonBean<String> uploadImage(@RequestPart("image") MultipartFile file, MultipartFileParamDto dto) throws IOException {
        return fileService.uploadImage(file, dto);
    }

    @Operation(summary = "图片下载", description = "...")
    @EnableAutoLog
    @GetMapping(value = "/image/download/{imageId}")
    public JsonBean<Void> downloadImage(@PathVariable("imageId") Long imageId, HttpServletResponse response) {
        return fileService.downloadImage(imageId, response);
    }

}
