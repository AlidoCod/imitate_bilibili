package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;

@TableName(value = "image")
@EqualsAndHashCode(callSuper = true)
@Data
public class Image extends BaseEntity {

    String md5;
    String imagePath;
}
