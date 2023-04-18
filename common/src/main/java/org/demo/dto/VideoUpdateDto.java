package org.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "视频信息请求体")
@Data
public class VideoUpdateDto {

    @Schema(description = "视频ID")
    Long id;
    @Schema(description = "更新的封面ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long imageId;
    @Schema(description = "更新的合集ID")
    Long seriesId;
    @Schema(description = "视频标题")
    String title;
    @Schema(description = "视频描述")
    String description;

}
