package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;


@TableName("video")
@Data
@EqualsAndHashCode(callSuper = true)
public class Video extends BaseEntity {

    Long seriesId;
    Long imageId;
    String md5;
    String title;
    String description;
    String videoPath;
    String videoSuffix;
    Long size;
}
