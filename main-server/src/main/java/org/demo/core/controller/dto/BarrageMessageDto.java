package org.demo.core.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "弹幕请求体")
@Data
public class BarrageMessageDto {

    @Schema(description = "视频ID")
    Long videoId;
    @Schema(description = "弹幕消息")
    String message;
}
