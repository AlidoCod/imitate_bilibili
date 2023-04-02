package org.demo.core.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.demo.core.aop.annotation.EnableAutoLog;
import org.demo.core.util.IPUtil;
import org.demo.core.util.ThreadHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

@Order(value = 1)
@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class EnableAutoLogAspect {

    private final ObjectMapper objectMapper;

    private final ThreadPoolExecutor threadPoolExecutor;

    @Around(value = "@annotation(org.demo.core.aop.annotation.EnableAutoLog)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前线程的资源，便于使用线程池异步输出日志
        HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String requestInformation = ThreadHolder.getLog();

        FutureTask<String> futureTask = new FutureTask<String>(() -> {
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

            //3. IP地址 + 用户名
            String userLog = "【IP: " + IPUtil.getIpAddress(request) +
                    "】 " + requestInformation;

            return "\r\nrequest method: " + classAndMethodName + "\r\nrequest params: " + argsJson
                    + "\r\nrequest information: " + userLog + "\r\ndescription: " + Arrays.toString(messages);
        });

        threadPoolExecutor.submit(futureTask);

        //4. 程序执行时间
        long begin = System.currentTimeMillis();

        // 先捕获异常，避免异常中断日志输出，等日志输出后，抛出异常
        Throwable ex = null;
        Object object = null;
        try {
            object = joinPoint.proceed();
        } catch (Throwable e) {
            ex = e;
        }

        long execTime = System.currentTimeMillis() - begin;

        // 异步输出日志
        threadPoolExecutor.submit(() -> {
            String message = null;
            try {
                message = futureTask.get() + "\r\nmethod exec time: " + execTime + "ms";
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            log.info(message);
        });

        // 若有异常则抛出异常，否则正常返回。
        if (ex != null) throw ex;
        return object;
    }

}
