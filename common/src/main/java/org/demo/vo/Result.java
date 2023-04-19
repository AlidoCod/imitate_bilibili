package org.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.demo.constant.EntityConstant;
import org.demo.pojo.base.GlobalRuntimeException;
import org.demo.pojo.base.ResponseEnum;

/**
 * Json的返回解析依赖于Get方法
 * @param <T>
 */
@Schema(description = "返回体")
@Data
@AllArgsConstructor
public class Result<T> {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "状态码")
    int code;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "返回信息")
    String message;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "返回数据")
    T data;

    public Result() {}

    private Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    private Result(ResponseEnum responseEnum) {
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    /**
     * 常用的success方法封装, msg代表成功
     * @return  (200, msg)
     */
    public static Result<Void> success() {
        return of(200, "success");
    }

    /**
     * 常用的success方法封装, data代表成功
     */
    public static <T> Result<T> successByData(T data) {
        return new Result<>(200, data);
    }

    /**
     * 通用的异常封装，controller用不到，由全局异常捕获
     */
    public static Result<Void> responseEnum(ResponseEnum responseEnum) {
        return new Result<>(responseEnum);
    }

    /**
     * 静态构造方法
     */
    public static Result<Void> of(int code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> of(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 全局异常捕获到Exception
     */
    public static Result<Void> error() {
        return new Result<>(500, "服务器内部未知错误，请联系管理员");
    }

    public static Result<Void> fail() {
        return of(200, "fail");
    }

    public static Result<Void> fail(String message) {
        return of(200, message);
    }

    public static Result<Void> success(String message) {
        return of(200, message);
    }

    public static Result<Void> yes () {
        return of(200, EntityConstant.YES);
    }

    public static Result<Void> no () {
        return of(200, EntityConstant.NO);
    }

    public static Result<Void> ok() {
        return of(200, "ok");
    }
    /**
     * 全局异常捕获到GlobalRuntimeException
     */
    public static Result<Void> globalRuntimeException(GlobalRuntimeException exception) {
        return new Result<>(exception.getCode(), exception.getMessage());
    }
}
