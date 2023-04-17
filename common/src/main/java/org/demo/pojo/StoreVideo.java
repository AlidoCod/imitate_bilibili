package org.demo.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.pojo.base.BaseEntity;


@EqualsAndHashCode(callSuper = true)
@Data
@TableName("store_video")
public class StoreVideo extends BaseEntity {

    Long userId;
    Long storeId;
    Long videoId;
}
