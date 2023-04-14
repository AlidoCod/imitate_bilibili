package org.demo.core.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("comment")
public class Comment extends BaseEntity {

    Long slaveUserId;
    Long masterUserId;
    Long videoId;
    String content;
}
