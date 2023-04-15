package org.demo.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.demo.core.pojo.enums.ResponseEnum;
import org.demo.exception.GlobalRuntimeException;

/**
 * Json的返回解析依赖于Get方法
 * @param <T>
 */
@Schema(description = "返回体")
@Data
@AllArgsConstructor
public class JsonBean<T> {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "状态码")
    int code;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "返回信息")
    String message;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "返回数据")
    T data;

    public JsonBean() {}

    private JsonBean(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private JsonBean(int code, T data) {
        this.code = code;
        this.data = data;
    }

    private JsonBean(ResponseEnum responseEnum) {
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    /**
     * 常用的success方法封装, msg代表成功
     * @return  (200, msg)
     */
    public static JsonBean<Void> success() {
        return new JsonBean<>(ResponseEnum.SUCCESS);
    }

    /**
     * 常用的success方法封装, data代表成功
     */
    public static <T> JsonBean<T> successByData(T data) {
        return new JsonBean<>(200, data);
    }

    /**
     * 常用的fail方法封装
     * @return (500, msg)
     */
    public static JsonBean<Void> fail() {
        return new JsonBean<>(ResponseEnum.FAIL);
    }

    /**
     * 通用的异常封装，controller用不到，由全局异常捕获
     */
    public static JsonBean<Void> responseEnum(ResponseEnum responseEnum) {
        return new JsonBean<>(responseEnum);
    }

    /**
     * 静态构造方法
     */
    public static JsonBean<Void> of(int code, String message) {
        return new JsonBean<>(code, message);
    }

    public static <T> JsonBean<T> of(int code, String message, T data) {
        return new JsonBean<>(code, message, data);
    }

    /**
     * 全局异常捕获到Exception
     */
    public static JsonBean<Void> error() {
        return new JsonBean<>(500, "服务器内部错误，请联系管理员");
    }

    public static JsonBean<Void> fail(String message) {
        return of(400, message);
    }

    public static JsonBean<Void> success(String message) {
        return of(200, message);
    }
    /**
     * 全局异常捕获到GlobalRuntimeException
     */
    public static JsonBean<Void> globalRuntimeException(GlobalRuntimeException exception) {
        return new JsonBean<>(exception.getCode(), exception.getMessage());
    }
}
