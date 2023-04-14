package org.demo.core.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.aop.annotation.EnableAutoLog;
import org.demo.core.controller.dto.LoginDto;
import org.demo.core.controller.dto.RegisterDto;
import org.demo.core.service.AuthenticationService;
import org.demo.core.service.SimpleApiService;
import org.demo.core.controller.vo.JsonBean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Slf4j
@Validated
@Tag(name = "认证接口")
@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final SimpleApiService simpleApiService;

    @Operation(summary = "验证码", description = "生成图片验证码, 15分钟有效")
    @ApiResponse(responseCode = "200", description = "返回一个jpg格式的图片")
    @EnableAutoLog
    @ResponseBody
    @GetMapping(value = "/verify-code")
    public JsonBean<Void> verifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        simpleApiService.generateVerifyCode(request, response);
        return JsonBean.success();
    }

    @Operation(summary = "登录接口", description = "根据用户名、密码和验证码进行用户的登录，前面没有验证码切记不能请求，否则报错")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "请求成功, 其余的都是发生了异常")
    })
    @EnableAutoLog
    @ResponseBody
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public JsonBean<String> login(@RequestBody LoginDto loginDTO, HttpServletRequest request) throws Exception {
        return JsonBean.successByData(authenticationService.authenticate(loginDTO, request));
    }

    @Operation(summary = "注册接口", description = "手机号、密码、验证码和注册码，注册码为123456（贫穷没话费）")
    @ApiResponse(responseCode = "200", description = "注册成功，则返回token，前端加上token后可直接访问其他页面")
    @EnableAutoLog
    @ResponseBody
    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public JsonBean<String> register(@RequestBody RegisterDto registerDto, HttpServletRequest request) throws Exception {
        return JsonBean.successByData(authenticationService.register(registerDto, request));
    }

    /**
     * 请求转发接口，隐藏就好
     */
    @Hidden
    @GetMapping(value = "/doc")
    public String doc() {
        return "forward:/doc.html";
    }
}
