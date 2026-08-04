package com.zh.hengyi.common.result;

/**
 * 全局响应状态码枚举
*/
public enum ResultCode {

    // 成功
    SUCCESS(200, "success"),
    // 失败
    ERROR(500, "操作失败"),

    // 参数异常
    PARAM_ERROR(400, "请求参数错误"),


    // 用户相关 41
    USER_ERROR(4100,"数据库异常"),
    USER_EXIST(4101,"用户已存在"),
    USER_NOT_EXIST(4102,"用户不存在"),
    USERNAME_EXIST(4103,"用户名已存在"),
    USER_STATUS_FORBIDDEN(4104,"用户被禁用"),
    PASSWORD_ERROR(4105,"密码错误"),
    LOGIN_SERIALIZER_ERROR(4106,"登录信息序列化失败"),
    LOGIN_NOT_EXIST(4107,"用户未登录"),

    // 数据库相关 42
    DB_ERROR(4200,"数据库异常"),
    // 唯一索引重复，数据重复插入
    DB_DUPLICATE_KEY(4201, "数据重复，请勿重复提交"),
    // 字段约束校验失败（非空、长度、格式不符合表结构约束）
    DB_FIELD_CONSTRAINT_ERROR(4202, "字段不符合数据库约束要求"),
    // 外键约束异常，存在关联数据无法删除
    DB_FOREIGN_KEY_ERROR(4203, "存在关联数据，禁止删除"),
    // 数据库连接异常
    DB_CONNECT_ERROR(4204, "数据库连接异常，请稍后重试"),
    // SQL语法错误
    DB_SQL_GRAMMAR_ERROR(4205, "SQL语句执行异常"),
    // 事务执行失败
    DB_TRANSACTION_ERROR(4206, "事务执行失败，操作已回滚"),
    // 数据库通用未知异常兜底
    DB_COMMON_ERROR(4299, "数据库执行异常，请联系管理员");



    private final Integer code;
    private final String msg;


    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}