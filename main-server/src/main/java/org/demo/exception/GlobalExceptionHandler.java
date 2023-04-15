package org.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.demo.controller.vo.JsonBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 使用warn型警告，因为这是预期之中的异常
     */
    @ExceptionHandler(value = GlobalRuntimeException.class)
    public JsonBean<Void> globalRuntimeException(GlobalRuntimeException ex) {
        log.warn(ex.getMessage(), ex);
        return JsonBean.of(ex.getCode(), ex.getMessage());
    }

    /**
     * 使用error型警告，这是意料之外的异常
     */
    @ExceptionHandler(value = Exception.class)
    public JsonBean<Void> exception(Exception ex) {
        log.error(ex.getMessage(), ex);
        return JsonBean.error();
    }
}
