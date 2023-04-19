package org.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.service.LikesService;
import org.demo.util.ThreadHolder;
import org.demo.vo.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "点赞模块")
@RequestMapping("/likes")
@RestController
public class LikesController {

    private final LikesService likesService;

    @Operation(summary = "点赞")
    @EnableAutoLog
    @GetMapping("/like")
    public Result<Void> like(@RequestParam("videoId") Long videoId) {
        return likesService.like(ThreadHolder.getUser().getId(), videoId);
    }

    @Operation(summary = "取消点赞")
    @EnableAutoLog
    @GetMapping("/unlike")
    public Result<Void> unlike(@RequestParam("videoId") Long videoId) {
        return likesService.unlike(ThreadHolder.getUser().getId(), videoId);
    }

    @Operation(summary = "是否点赞")
    @EnableAutoLog
    @GetMapping("/is-like")
    public Result<Void> isLike(@RequestParam("videoId") Long videoId) {
        return likesService.isLike(ThreadHolder.getUser().getId(), videoId);
    }

    @Operation(summary = "视频点赞数量")
    @EnableAutoLog
    @GetMapping("/nums/video")
    public Result<Void> oneVideoLikesNums(@RequestParam("videoId") Long videoId) {
        return likesService.oneVideoLikesNums(videoId);
    }

    @Operation(summary = "用户视频点赞数量")
    @EnableAutoLog
    @GetMapping("/nums/user")
    public Result<Void> userVideoLikesNums(@RequestParam("userId") Long userId) {
        return likesService.userVideoLikesNums(userId);
    }

    @Operation(summary = "自己的视频总点赞数量")
    @EnableAutoLog
    @GetMapping("/nums/user/me")
    public Result<Void> myVideoLikesNums() {
        return likesService.userVideoLikesNums(ThreadHolder.getUser().getId());
    }

    @Operation(summary = "系列视频总点赞数量")
    @EnableAutoLog
    @GetMapping("/nums/series")
    public Result<Void> seriesVideoNums(@RequestParam("seriesId") Long seriesId) {
        return likesService.seriesVideoNums(seriesId);
    }
}
