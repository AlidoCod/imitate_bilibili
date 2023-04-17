package org.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "弹幕请求体")
@Data
public class BarrageMessageDto {

    @Schema(description = "视频ID")
    Long videoId;
    @Schema(description = "弹幕消息")
    String message;
    @Schema(description = "弹幕对应的视频时间，后端设置为字符串，你只需要传前端方便处理的数据即可")
    String videoTime;
}
