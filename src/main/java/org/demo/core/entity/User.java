package org.demo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.demo.core.entity.base.BaseEntity;
import org.demo.core.entity.enums.Role;
import org.demo.core.entity.enums.Tag;
import org.demo.core.handler.ListTagTypeHandler;

import java.util.List;

/**
 * user表对应的实体类
 * 不能直接使用父类的hashcode和equal方法，否则只会比较父类属性
 */
@EqualsAndHashCode(callSuper = true)
@TableName("user")
@Data
public class User extends BaseEntity {

    Long imageId;
    String username;
    String nickname;
    // 忽略密码字段，避免泄露密码
    String password;
    String email;
    @TableField(typeHandler = ListTagTypeHandler.class)
    List<Tag> tags;
    String signature;
    Role role;
    // 设置逻辑删除字段
    Character status;

}
