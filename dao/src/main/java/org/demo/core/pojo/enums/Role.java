package org.demo.core.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum Role {

    User(0, "user"),
    Admin(1, "admin");

    @EnumValue
    private final int code;

    private final String value;

    private Role(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
