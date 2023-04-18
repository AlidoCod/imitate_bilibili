package org.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.dto.SeriesCreateDto;
import org.demo.dto.SeriesUpdateDto;
import org.demo.pojo.Series;
import org.demo.service.SeriesService;
import org.demo.util.ThreadHolder;
import org.demo.vo.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 @Operation(summary = "用户关注")
 @EnableAutoLog
 @GetMapping(value = "/follow")
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "合集接口")
@RequestMapping(value = "/series")
@RestController
public class SeriesController {

    private final SeriesService seriesService;

    @Operation(summary = "创建合集")
    @EnableAutoLog
    @PostMapping(value = "/create")
    public Result<Void> create(@RequestBody SeriesCreateDto dto) throws JsonProcessingException {
        return seriesService.create(dto);
    }

    @Operation(summary = "查询合集")
    @EnableAutoLog
    @GetMapping(value = "/query")
    public Result<List<Series>> query() {
        return seriesService.query(ThreadHolder.getUser().getId());
    }

    @Operation(summary = "删除合集")
    @EnableAutoLog
    @GetMapping(value = "/delete")
    public Result<Void> delete(@RequestParam("id")Long id) {
        return seriesService.delete(id);
    }

    @Operation(summary = "更新合集")
    @EnableAutoLog
    @PostMapping(value = "/update")
    public Result<Void> update(@RequestBody SeriesUpdateDto dto) throws JsonProcessingException {
        return seriesService.update(dto);
    }

    @Operation(summary = "关注合集")
    @EnableAutoLog
    @GetMapping(value = "/follow")
    public Result<Void> follow(@RequestParam("seriesId") Long seriesId) {
        return seriesService.follow(ThreadHolder.getUser().getId(), seriesId);
    }

    @Operation(summary = "取关合集")
    @EnableAutoLog
    @GetMapping(value = "/unfollow")
    public Result<Void> unfollow(@RequestParam("seriesId") Long seriesId) {
        return seriesService.unfollow(ThreadHolder.getUser().getId(), seriesId);
    }

    @Operation(summary = "合集关注人数")
    @EnableAutoLog
    @GetMapping(value = "/followers-nums")
    public Result<Long> followersNums(@RequestParam("seriesId") Long seriesId) {
        return seriesService.followersNums(seriesId);
    }
}
