package org.demo.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.demo.core.util.JsonBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = GlobalRuntimeException.class)
    public JsonBean<Void> globalRuntimeException(GlobalRuntimeException ex) {
        log.warn(ex.getMessage(), ex);
        return JsonBean.of(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public JsonBean<Void> exception(Exception ex) {
        log.error(ex.getMessage(), ex);
        return JsonBean.error();
    }
}
