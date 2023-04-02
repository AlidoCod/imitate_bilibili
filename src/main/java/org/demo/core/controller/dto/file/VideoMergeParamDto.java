package org.demo.core.controller.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.demo.core.entity.enums.Tag;

import java.util.List;

@Schema(description = "视频上传请求体")
@Data
public class VideoMergeParamDto {

    @Min(1)
    @NotNull
    @Schema(description = "分块最大下标", example = "9, 代表上传了1-9分块")
    Integer maxIndex;
    @Schema(description = "合集（系列）对应的ID")
    Long seriesId;
    @NotBlank
    @Schema(description = "封面图片对应的ID")
    Long imageId;
    @NotBlank
    @Schema(description = "文件的md5")
    String md5;
    @NotBlank
    @Schema(description = "视频的标题")
    String title;
    @Schema(description = "简介")
    String description = "";
    @NotBlank
    @Schema(description = "视频的标签，至少一个不能查无此屏")
    List<Tag> tags;
    @NotBlank
    @Schema(description = "视频的后缀", example = ".mp4")
    String suffix;
    @NotBlank
    @Schema(description = "视频的字节大小，以Byte为单位", example = "1024")
    Long size;
}
