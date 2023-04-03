package org.demo.core.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("appreciation")
public class Appreciation extends BaseEntity {

    Long userId;
    Long videoId;
}
