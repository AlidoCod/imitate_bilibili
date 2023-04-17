package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("follow_series")
public class FollowSeries extends BaseEntity {

    Long userId;
    Long seriesId;
}
