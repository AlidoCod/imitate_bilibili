package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.handler.ListTagTypeHandler;
import org.demo.pojo.base.BaseEntity;
import org.demo.pojo.base.Tag;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("series")
public class Series extends BaseEntity {

    Long userId;
    Long imageId;
    String title;
    String description;
    @TableField(typeHandler = ListTagTypeHandler.class)
    List<Tag> tags;
}
