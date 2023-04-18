package org.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.pojo.base.Tag;

import java.util.List;

@Data
public class SeriesUpdateDto {

    @Schema(description = "合集ID")
    @NotBlank
    Long id;
    @Schema(description = "封面ID")
    Long imageId;
    @Schema(description = "标题")
    String title;
    @Schema(description = "简介")
    String description;
    @Schema(description = "标签")
    List<Tag> tags;

}
