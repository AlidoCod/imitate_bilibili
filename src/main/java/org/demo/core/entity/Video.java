package org.demo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.entity.base.BaseEntity;
import org.demo.core.entity.enums.Tag;
import org.demo.core.handler.ListTagTypeHandler;

import java.util.List;

@TableName("video")
@Data
@EqualsAndHashCode(callSuper = true)
public class Video extends BaseEntity {

    Long seriesId;
    Long imageId;
    String md5;
    String title;
    String description;
    @TableField(typeHandler = ListTagTypeHandler.class)
    List<Tag> tags;
    String videoPath;
    String videoSuffix;
    Long size;
}
