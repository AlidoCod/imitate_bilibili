package org.demo.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.aop.annotation.EnableAutoLog;
import org.demo.core.controller.dto.BarrageMessageDto;
import org.demo.core.controller.vo.JsonBean;
import org.demo.core.service.BarrageService;
import org.demo.core.util.ThreadHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "弹幕接口")
@RequestMapping(value = "/barrage")
@RestController
public class BarrageController {

    private final BarrageService barrageService;

    @Operation(summary = "发送弹幕", description = "建立WebSocket连接且传输videoId初始化后再调用此接口")
    @ApiResponse(responseCode = "200", description = "返回一个jpg格式的图片")
    @EnableAutoLog
    @ResponseBody
    @PostMapping(value = "/sent")
    public JsonBean<Void> sent(@RequestBody BarrageMessageDto messageDto) throws IOException {
        return barrageService.sent(ThreadHolder.getUser().getId(), messageDto.getVideoId(), messageDto.getMessage());
    }
}
