package org.demo.core.controller.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "文件参数，从前端获取，避免后端XXS注入")
@Data
public class MultipartFileParamDto {

    @NotBlank
    @Schema(description = "前端计算的文件MD5")
    String md5;
    @NotBlank
    @Schema(description = "文件后缀", defaultValue = ".jpg")
    String suffix;
}
