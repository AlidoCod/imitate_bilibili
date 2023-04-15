package org.demo.core.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.pojo.base.BaseEntity;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_store_video")
public class UserStoreVideo extends BaseEntity {

    Long userId;
    Long storeId;
    Long videoId;
}
