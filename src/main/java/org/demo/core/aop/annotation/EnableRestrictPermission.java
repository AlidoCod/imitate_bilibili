package org.demo.core.aop.annotation;

import org.demo.core.entity.enums.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限限制注解, 由于时间及耐心问题，放弃了权限空值，此注解也失去了意义。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRestrictPermission {

    /**
     * 需要限制权限的角色，比如管理员角色就Role.Admin，同时这也是默认值
     */
    Role[] roles() default {Role.Admin};
}
