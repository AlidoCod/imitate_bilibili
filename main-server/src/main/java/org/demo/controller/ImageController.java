package org.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.dto.file.MultipartFileParamDto;
import org.demo.service.ImageService;
import org.demo.vo.Result;
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
@Tag(name = "图片文件信息")
@RequestMapping("/image")
@RestController
public class ImageController {

    private final ImageService fileService;

    @Operation(summary = "图片MD5校验", description = "图片上传前确保图片在数据库/文件系统不存在")
    @ApiResponse(responseCode = "200", description = "成功时，返回imageId")
    @EnableAutoLog
    @GetMapping("/check")
    public Result<Void> checkImage(@RequestParam("md5") String md5) {
        return fileService.checkImage(md5);
    }

    @Operation(summary = "图片/封面上传")
    @EnableAutoLog
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Result<String> uploadImage(@RequestPart("image") MultipartFile file, MultipartFileParamDto dto) throws IOException {
        return fileService.uploadImage(file, dto);
    }

    @Operation(summary = "图片下载", description = "...")
    @EnableAutoLog
    @GetMapping(value = "/download/{imageId}")
    public Result<Void> downloadImage(@PathVariable("imageId") Long imageId, HttpServletResponse response) {
        return fileService.downloadImage(imageId, response);
    }

}
