package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("fan")
public class Fan extends BaseEntity {

    Long userId;
    Long followId;
}
