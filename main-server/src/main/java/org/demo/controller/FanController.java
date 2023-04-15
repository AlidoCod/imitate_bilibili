package org.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.controller.vo.InterestVo;
import org.demo.service.FanService;
import org.demo.controller.vo.JsonBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "用户信息接口")
@RequestMapping(value = "/fan")
@RestController
public class FanController {

    private final FanService fanService;

    @Operation(summary = "用户关注")
    @EnableAutoLog
    @GetMapping(value = "/interest/{id}")
    public JsonBean<Void> interest(@PathVariable("id") Long id) {
        return fanService.interest(id);
    }

    @Operation(summary = "用户取消关注")
    @EnableAutoLog
    @GetMapping(value = "/not-interest/{id}")
    public JsonBean<Void> notInterest(@PathVariable("id") Long id) {
        return fanService.notInterest(id);
    }

    @Operation(summary = "获取关注列表")
    @EnableAutoLog
    @GetMapping(value = "/interest")
    public JsonBean<Page<InterestVo>> interestList (@RequestParam("pageNum")Integer pageNum, @RequestParam("pageSize")Integer pageSize) {
        return fanService.getInterestList(pageNum, pageSize);
    }
}
