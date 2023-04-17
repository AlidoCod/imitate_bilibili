package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.handler.ListTagTypeHandler;
import org.demo.pojo.base.BaseEntity;
import org.demo.pojo.base.Tag;

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
