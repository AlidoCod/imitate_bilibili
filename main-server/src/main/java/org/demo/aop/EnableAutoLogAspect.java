package org.demo.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.demo.aop.annotation.EnableAutoLog;
import org.demo.core.pojo.User;
import org.demo.util.IPUtil;
import org.demo.util.ThreadHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Order(value = 1)
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class EnableAutoLogAspect {

    private final ObjectMapper objectMapper;

    @Around(value = "@annotation(org.demo.aop.annotation.EnableAutoLog)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        log.debug("=================================begin===============================");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取反射方法
        Method method = signature.getMethod();
        // 获取注解
        EnableAutoLog enableAutoLog = method.getAnnotation(EnableAutoLog.class);
        String[] messages = enableAutoLog.messages();

        // 1. 获取请求类+方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        String classAndMethodName = className + "_" + methodName;

        log.debug("【method】: {}", classAndMethodName);

        // 2. 获取请求参数, JSON解析前的参数
        Object[] args = joinPoint.getArgs();
        List<Object> argsList=new ArrayList<>();
        for (Object arg : args) {
            // 排除这两种参数类型
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            argsList.add(arg);
        }
        String argsJson = objectMapper.writeValueAsString(argsList);

        log.debug("【request params】: {}", argsJson);

        User user = ThreadHolder.getUser();
        //3. IP地址 + 用户名
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String userLog = "【IP: " + IPUtil.getIpAddress(request) +
                "】 " + "username: " + (user == null ? "NULL" : user.getUsername());

        log.debug(userLog);

        //4. 程序执行时间
        long begin = System.currentTimeMillis();
        Object object = joinPoint.proceed();
        log.debug("【exec time】: {}ms", System.currentTimeMillis() - begin);

        log.debug("【extra message】: {}", messages);

        log.debug("=================================end===============================");
        return object;
    }

}
