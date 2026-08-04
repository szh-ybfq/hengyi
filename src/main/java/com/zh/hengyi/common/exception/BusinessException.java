package com.zh.hengyi.common.exception;


import com.zh.hengyi.common.result.ResultCode;

/**
 * 自定义业务异常
 * 为什么写他？因为throw new RuntimeException("账号密码错误");只能传一段文字，没有状态码
 */
public class BusinessException extends RuntimeException {

    // 新增：业务状态码（在ResultCode枚举里面统一管理）
    private final Integer code;

    // 这段是构造方法，创建业务异常时传入状态码
    public BusinessException(ResultCode ResultCode) {
        super(ResultCode.getMsg());
        this.code = ResultCode.getCode();
    }

    // 创建业务异常时传入状态码、异常消息
    public BusinessException(ResultCode ResultCode, String msg) {
        super(msg);
        this.code = ResultCode.getCode();
    }

    // 完全自定义code+msg（少数动态场景使用）
//    public BusinessException(Integer code, String msg) {
//        super(msg);
//        this.code = code;
//    }

    public Integer getCode() {
        return code;
    }
}
