package com.zh.hengyi.common.result;

/**
 * 全局响应状态码枚举
*/
public enum ResultCode {

    // 成功
    SUCCESS(200, "success"),
    // 失败
    ERROR(500, "操作失败"),

    // 参数异常 40
    PARAM_ERROR(400, "请求参数错误"),
    VALID_PARAM_ERROR(4001, "请求参数错误"),

    // 用户相关 41
    USER_ERROR(4100,"用户异常"),
    USER_EXIST(4101,"用户已存在"),
    USER_NOT_EXIST(4102,"用户不存在"),
    USERNAME_EXIST(4103,"用户名已存在"),
    USER_STATUS_LOCKED(4104,"用户被锁定"),
    USER_STATUS_FORBIDDEN(4105,"用户被禁用"),
    USER_LOGIN_AUTH_FAIL(4106,"用户认证失败"),
    PASSWORD_ERROR(4107,"密码错误"),
    LOGIN_SERIALIZER_ERROR(4108,"登录信息序列化失败"),
    LOGIN_NOT_EXIST(4109,"用户未登录"),
    ADMIN_NOT_DELETE(4110,"超级管理员不能删除"),

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
    DB_COMMON_ERROR(4299, "数据库执行异常，请联系管理员"),

    // 菜单相关 43
    MENU_NOT_EXIST(4301,"菜单不存在"),
    MENU_PARENT_NOT_SELF(4302,"父菜单不能选择自己"),
    ROLE_NOT_EXIST(4303,"角色不存在"),
    ADMIN_ROLE_NOT_DELETE(4304,"内置超级管理员角色不可删除"),

    // 商品、分类 44
    // 4401 分类不存在
    CATEGORY_NOT_EXIST(4401, "该分类不存在"),
    // 4402 父分类不存在
    CATEGORY_PARENT_NOT_EXIST(4402, "父分类不存在"),
    // 4403 分类名称已存在
    CATEGORY_NAME_DUPLICATE(4403, "该分类名称已存在"),
    // 4404 父分类不能选择自己
    CATEGORY_PARENT_NOT_SELF(4404, "父分类不能选择自己"),
    // 4405 存在子分类禁止删除
    CATEGORY_HAS_CHILD_NOT_DELETE(4405, "删除失败：该分类下存在子分类，请先删除子分类"),
    // 4406 分类绑定商品禁止删除
    CATEGORY_HAS_SPU_NOT_DELETE(4406, "删除失败：该分类下存在商品数据，请先迁移/删除商品"),
    // 4421 商品不存在
    SPU_NOT_EXIST(4421, "商品不存在"),
    // 4422 商品名称重复
    SPU_NAME_DUPLICATE(4422, "商品名称已存在"),
    // 4423 SKU规格不能为空
    SPU_SKU_EMPTY(4423, "至少填写一条SKU规格");
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