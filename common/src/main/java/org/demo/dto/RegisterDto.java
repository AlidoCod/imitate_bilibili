package org.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "注册请求体")
@Data
public class RegisterDto {

    @Schema(description = "用户名即手机号，邮箱不可注册但可用于登录")
    @NotBlank
    String username;

    @Schema(description = "登录密码")
    @NotBlank
    String password;

    @Schema(description = "验证码")
    @NotBlank
    String verifyCode;

    @Schema(description = "注册码, 后端写死，手机注册码")
    @NotBlank
    String registerCode;
}
