package com.zh.hengyi.common.result;

import lombok.Data;

/**
 * 全局统一返回结果
 */
@Data
public class Result<T> {

    // 响应码 
    private Integer code;
    // 响应信息 
    private String msg;
    // 业务数据 
    private T data;

    //  1 成功
    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(String msg, T data) { //返回自定义成功消息，数据
        return build(ResultCode.SUCCESS.getCode(), msg, data);
    }

    //  2 失败
    public static <T> Result<T> error() {
        return build(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg(), null);
    }

    public static <T> Result<T> error(String msg) {
        return build(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> error(Integer code,String msg) {
        return build(code, msg, null);
    }


    //  构造方法
    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

}