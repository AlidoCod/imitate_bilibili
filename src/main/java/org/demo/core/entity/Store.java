package org.demo.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.entity.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("store")
public class Store extends BaseEntity {

    Long userId;
    Long imageId;
    String description;
}
