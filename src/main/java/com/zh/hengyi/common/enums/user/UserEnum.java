package com.zh.hengyi.common.enums.user;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public enum UserEnum {

    // 状态启用
    STATUS_NORMAL(0, "用户状态正常"),
    // 状态禁用
    STATUS_FORBIDDEN(1, "用户被禁用");

    private final Integer code;
    private final String msg;


    UserEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        boolean a = true;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
