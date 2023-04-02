package org.demo.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.entity.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_series")
public class UserSeries extends BaseEntity {

    Long userId;
    Long seriesId;
}
