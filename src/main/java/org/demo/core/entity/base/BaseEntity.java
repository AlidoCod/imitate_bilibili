package org.demo.core.entity.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    // 使用雪花算法ID递增
    @TableId(type = IdType.ASSIGN_ID)
    Long id;

    @TableField(fill = FieldFill.INSERT)
    LocalDateTime createTime;
}
