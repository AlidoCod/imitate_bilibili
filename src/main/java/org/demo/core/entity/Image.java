package org.demo.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.entity.base.BaseEntity;

@TableName(value = "image")
@EqualsAndHashCode(callSuper = true)
@Data
public class Image extends BaseEntity {

    String md5;
    String imagePath;
}
