package org.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.demo.aop.annotation.EnableRestrictPermission;
import org.demo.core.pojo.enums.ResponseEnum;
import org.demo.core.pojo.enums.Role;
import org.demo.exception.GlobalRuntimeException;
import org.demo.util.ThreadHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限控制切面，注意@Order值越小的切面，越内层
 * 此切面的Order值一定要小于日志切面，确保日志切面可以成功输出
 */
@Order(value = 0)
@Component
@Aspect
public class EnableRestrictPermissionAspect {

    @Around(value = "@annotation(org.demo.aop.annotation.EnableRestrictPermission)")
    public Object restrictPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取反射方法
        Method method = signature.getMethod();
        // 获取注解
        EnableRestrictPermission enableRestrictPermission= method.getAnnotation(EnableRestrictPermission.class);
        Role[] roles = enableRestrictPermission.roles();
        Role userRole = ThreadHolder.getRole();
        // 枚举对象是单例唯一的
        for (Role role : roles) {
            if (userRole == role)
                return joinPoint.proceed();
        }
        throw GlobalRuntimeException.of(ResponseEnum.HTTP_STATUS_403);
    }
}
