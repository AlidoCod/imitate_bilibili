package org.demo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.demo.pojo.base.Tag;

import java.util.List;

@Schema(description = "用户更新请求体")
@Data
public class UserUpdateDto {

    @NotBlank
    String nickname;
    @Email
    String email;
    @NotNull
    List<Tag> tags;
    @NotBlank
    String signature;
}
