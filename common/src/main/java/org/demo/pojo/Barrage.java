package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;


@EqualsAndHashCode(callSuper = true)
@Data
@TableName("barrage")
public class Barrage extends BaseEntity {

    Long userId;
    Long videoId;
    String content;
    String videoTime;
}
