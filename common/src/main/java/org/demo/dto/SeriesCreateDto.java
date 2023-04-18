package org.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.pojo.base.Tag;

import java.util.List;

@Data
@Schema(description = "创建合集请求体")
public class SeriesCreateDto {

    @NotBlank
    @Schema(description = "封面ID")
    Long imageId;
    @NotBlank
    @Schema(description = "标题")
    String title;
    @Schema(description = "合集描述")
    String description;
    @NotBlank
    @Schema(description = "合集标签, 不能为空")
    List<Tag> tags;
}
