package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;


@EqualsAndHashCode(callSuper = true)
@Data
@TableName("store")
public class Store extends BaseEntity {

    Long userId;
    Long imageId;
    String description;
}
