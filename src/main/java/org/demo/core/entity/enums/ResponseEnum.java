package org.demo.core.entity.enums;

import lombok.Getter;

@Getter
public enum ResponseEnum {

    SUCCESS(200, "success"),
    FAIL(400, "failed"),

    HTTP_STATUS_200(200, "正常"),
    HTTP_STATUS_400(400, "坏请求，一个不存在的请求"),
    HTTP_STATUS_401(400, "没有认证，无法访问，请登录"),
    HTTP_STATUS_403(400, "权限不足"),
    HTTP_STATUS_500(500, "服务器内部异常，请联系管理员");

    private final int code;

    private final String message;

    private ResponseEnum(int i, String s) {
      this.code = i;
      this.message = s;
    }
}