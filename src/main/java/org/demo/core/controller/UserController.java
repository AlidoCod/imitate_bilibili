package org.demo.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.aop.annotation.EnableAutoLog;
import org.demo.core.controller.dto.file.MultipartFileParamDto;
import org.demo.core.controller.dto.user.UserUpdateDto;
import org.demo.core.controller.vo.UserVo;
import org.demo.core.service.UserService;
import org.demo.core.controller.vo.JsonBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "用户信息接口")
@RequestMapping(value = "/user")
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户信息查询接口", description = "...")
    @EnableAutoLog
    @GetMapping("/query")
    public JsonBean<UserVo> query() throws Exception {
        return userService.query();
    }

    @Operation(summary = "用户注销接口", description = "此接口尽量不要使用，使用了也不要重新注册，因为后台采用了逻辑删除，注销后再重新注册的逻辑懒得写了")
    @EnableAutoLog
    @GetMapping ("/delete")
    public JsonBean<Void> delete() throws Exception {
        userService.delete();
        return JsonBean.success();
    }

    @Operation(summary = "更新用户信息", description = "...")
    @EnableAutoLog
    @PostMapping("/update")
    public JsonBean<Void> update(@RequestBody UserUpdateDto dto) throws Exception {
        userService.update(dto);
        return JsonBean.success();
    }

    @Operation(summary = "用户头像异步上传", description = "...")
    @EnableAutoLog
    @PostMapping("/cover/update")
    public JsonBean<Void> coverUpdate(@RequestPart("image") MultipartFile file, MultipartFileParamDto dto) throws Exception {
        return userService.coverUpdate(file, dto);
    }

    @Operation(summary = "用户头像下载", description = "...")
    @EnableAutoLog
    @GetMapping("/cover/download")
    public JsonBean<Void> coverDownload(HttpServletResponse response) throws Exception {
        return userService.downloadCover(response);
    }

}
