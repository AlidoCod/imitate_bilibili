package org.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.dto.VideoUpdateDto;
import org.demo.dto.file.VideoMergeParamDto;
import org.demo.service.MultipartFileSender;
import org.demo.service.VideoService;
import org.demo.vo.Result;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "视频文件信息")
@RequestMapping("/video")
@RestController
public class VideoController {

    private final VideoService videoService;

    private final ApplicationContext applicationContext;
    @Operation(summary = "视频MD5校验", description = "视频上传前确保视频分块在数据库/文件系统不存在")
    @ApiResponse(responseCode = "200", description = "yes, 表示通过了检查，视频在数据库不存在; no表示未通过检查, 视频已存在, 不能重复上传")
    @EnableAutoLog
    @GetMapping("/check")
    public Result<Void> checkVideo(@RequestParam("md5") String md5) {
        return videoService.checkVideo(md5);
    }

    @Operation(summary = "视频分块MD5校验", description = "视频上传前确保视频分块在文件系统不存在")
    @ApiResponse(responseCode = "200", description = "yes, 表示通过检查；no，返回已存在的分块下标")
    @EnableAutoLog
    @GetMapping("/chunk/check")
    public Result<Void> checkVideoChunk(@RequestParam("md5") String md5, @RequestParam("index") int index) {
        return videoService.checkVideoChunk(md5, index);
    }

    @Operation(summary = "视频分块上传", description = "校验通过的分块不必重新上传，节省带宽，必须校验所有分块，后端无法确认分块状态")
    @ApiResponse(responseCode = "200")
    @EnableAutoLog
    @PostMapping(value = "/chunk/upload", consumes = "multipart/form-data")
    public Result<Void> uploadVideoChunk(@RequestPart("chunk") MultipartFile chunk, @RequestParam("md5") String md5, @RequestParam("index") int index) {
        return videoService.uploadVideoChunk(chunk, md5, index);
    }

    @Operation(summary = "视频分块合并", description = "请确保所有分块上传成功，上传失败的分块一定要重传")
    @ApiResponse(responseCode = "200")
    @EnableAutoLog
    @PostMapping(value = "/chunk/merge")
    public Result<Void> mergeVideoChunk(@RequestBody VideoMergeParamDto dto) {
        return videoService.mergeVideoChunk(dto);
    }

    @Operation(summary = "断点下载，利用Http Range")
    @EnableAutoLog
    @GetMapping(value = "/play/{videoId}")
    public void play(@PathVariable("videoId") @NotNull Long videoId) throws Exception {
        MultipartFileSender multipartFileSender = applicationContext.getBean("multipartFileSender", MultipartFileSender.class);
        multipartFileSender.sent(videoId);
    }

    @Operation(summary = "视频信息更新")
    @EnableAutoLog
    @PostMapping(value = "/update")
    public Result<Void> update(@RequestBody VideoUpdateDto dto) throws JsonProcessingException {
        return videoService.update(dto);
    }

    @Operation(summary = "视频信息删除")
    @EnableAutoLog
    @GetMapping(value = "/delete")
    public Result<Void> delete(@RequestParam("id")Long id) throws JsonProcessingException {
        return videoService.delete(id);
    }

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("^UPDATE:USER:ID::.*$");
        System.out.println(pattern.matcher("UPDATE:USER:ID::1671370755721060354").matches());
    }
}
