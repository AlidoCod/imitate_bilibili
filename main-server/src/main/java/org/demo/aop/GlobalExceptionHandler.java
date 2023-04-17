package org.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.demo.vo.Result;
import org.demo.pojo.GlobalRuntimeException;
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
    public Result<Void> globalRuntimeException(GlobalRuntimeException ex) {
        log.warn(ex.getMessage(), ex);
        return Result.of(ex.getCode(), ex.getMessage());
    }

    /**
     * 使用error型警告，这是意料之外的异常
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Void> exception(Exception ex) {
        log.error(ex.getMessage(), ex);
        return Result.error();
    }
}
