package org.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "登录请求体")
@Data
public class LoginDto {

    @Schema(description = "用户名即手机号或者邮箱都可以")
    @NotBlank
    String username;

    @Schema(description = "登录密码")
    @NotBlank
    String password;

    @Schema(description = "验证码")
    @NotBlank
    String verifyCode;
}
