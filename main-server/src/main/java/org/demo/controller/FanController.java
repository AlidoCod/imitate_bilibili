package org.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.constant.EntityConstant;
import org.demo.vo.Result;
import org.demo.vo.UserVo;
import org.demo.service.FollowService;
import org.demo.util.ThreadHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "用户关注接口")
@RequestMapping(value = "/relation")
@RestController
public class FanController {

    private final FollowService followService;

    @Operation(summary = "判断用户是否关注")
    @EnableAutoLog
    @GetMapping(value = "/is-follower")
    public Result<Void> isFollow(@RequestParam("followId") Long followId) {
        return Result.success(followService.isFollowed(ThreadHolder.getUser().getId(), followId) ? EntityConstant.YES : EntityConstant.NO);
    }

    @Operation(summary = "用户关注")
    @EnableAutoLog
    @GetMapping(value = "/follow")
    public Result<Void> follow(@RequestParam("followId") Long followId) {
        followService.follow(ThreadHolder.getUser().getId(), followId);
        return Result.success();
    }

    @Operation(summary = "用户取消关注")
    @EnableAutoLog
    @GetMapping(value = "/unfollow")
    public Result<Void> unfollow(@RequestParam("followId") Long followId) {
        followService.unfollow(ThreadHolder.getUser().getId(), followId);
        return Result.success();
    }

    @Operation(summary = "获取关注列表")
    @EnableAutoLog
    @GetMapping(value = "/followers-list")
    public Result<Page<UserVo>> followersList (@Parameter(description = "页号，需要第几页？")
                                                      @RequestParam(value = "current", defaultValue = "1")Integer current ,
                                                  @Parameter(description = "每页是多大?") @RequestParam(value = "size", defaultValue = "3")Integer size) {
        return Result.successByData(followService.getFollowers(new Page<>(current, size), ThreadHolder.getUser().getId()));
    }

    @Operation(summary = "获取粉丝列表")
    @EnableAutoLog
    @GetMapping(value = "/fans-list")
    public Result<Page<UserVo>> fansList (@Parameter(description = "页号，需要第几页？")
                                              @RequestParam(value = "current", defaultValue = "1")Integer current ,
                                          @Parameter(description = "每页是多大?") @RequestParam(value = "size", defaultValue = "3")Integer size) {
        return Result.successByData(followService.getFans(new Page<>(current, size), ThreadHolder.getUser().getId()));
    }

    @Operation(summary = "获取共同关注列表")
    @EnableAutoLog
    @GetMapping(value = "/collective-list")
    public Result<Page<UserVo>> collectiveFans (@Parameter(description = "页号，需要第几页？") @RequestParam(value = "current", defaultValue = "1")Integer current ,
                                         @Parameter(description = "每页是多大?") @RequestParam(value = "size", defaultValue = "3")Integer size,
                                                @Parameter(description = "对方的Id") @RequestParam(value = "userId") Long userId) {
        return Result.successByData(followService.getCollectiveFollower(new Page<>(current, size), ThreadHolder.getUser().getId(), userId));
    }

    @Operation(summary = "获取可能感兴趣的人")
    @EnableAutoLog
    @GetMapping(value = "/interesting-list")
    public Result<List<UserVo>> interestingList (@Parameter(description = "需要多少个?") @RequestParam(value = "num") Integer num) throws JsonProcessingException {
        return Result.successByData(followService.getInterestingFollower(ThreadHolder.getUser().getId(), num));
    }
}
