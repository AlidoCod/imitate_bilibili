package org.demo.core.exception;

import org.demo.core.pojo.enums.ResponseEnum;

public class GlobalRuntimeException extends RuntimeException{

    int code;

    private GlobalRuntimeException(int code, String message) {
        super(message);
        this.code = code;
    }

    private GlobalRuntimeException(String message, Exception e) {
        super(message, e);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static GlobalRuntimeException of(int code, String message) {
        return new GlobalRuntimeException(code, message);
    }

    public static GlobalRuntimeException of(ResponseEnum responseEnum) {
        return new GlobalRuntimeException(responseEnum.getCode(), responseEnum.getMessage());
    }

    public static GlobalRuntimeException of(String message) {
        return of(400, message);
    }

    public static GlobalRuntimeException of(String message, Exception e) {
        GlobalRuntimeException globalRuntimeException = new GlobalRuntimeException(message, e);
        globalRuntimeException.setCode(400);
        return globalRuntimeException;
    }
}
