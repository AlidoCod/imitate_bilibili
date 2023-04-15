package org.demo.core.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("series")
public class Series extends BaseEntity {

    Long userId;
    String title;
    String description;
}
