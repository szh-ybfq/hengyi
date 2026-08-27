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


    /**
     * 用户相关 41
     */
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

    /**
     * 数据库相关 42
     */
    DB_ERROR(4200,"数据库异常"),
    // 唯一索引重复，数据重复插入
    DB_DUPLICATE_KEY(4201, "数据重复，请勿重复提交"),
    // 字段约束校验失败（非空、长度、格式不符合表结构约束）
    DB_FIELD_CONSTRAINT_ERROR(4202, "字段不符合数据库约束要求"),
    // 外键约束异常，存在关联数据无法删除
    DB_FOREIGN_KEY_ERROR(4203, "存在关联数据，禁止删除"),
    DB_CONNECT_ERROR(4204, "数据库连接异常，请稍后重试"),
    DB_SQL_GRAMMAR_ERROR(4205, "SQL语句执行异常"),
    DB_TRANSACTION_ERROR(4206, "事务执行失败，操作已回滚"),
    DB_COMMON_ERROR(4299, "数据库执行异常，请联系管理员"),

    /**
     * 菜单相关 43
     */
    MENU_ERROR(4300,"菜单异常"),
    MENU_NOT_EXIST(4301,"菜单不存在"),
    MENU_PARENT_NOT_SELF(4302,"父菜单不能选择自己"),
    ROLE_NOT_EXIST(4303,"角色不存在"),
    ADMIN_ROLE_NOT_DELETE(4304,"内置超级管理员角色不可删除"),

    /**
     * 商品、分类 44
     */
    CATEGORY_ERROR(4400, "商品分类异常"),
    CATEGORY_NOT_EXIST(4401, "该分类不存在"),
    CATEGORY_PARENT_NOT_EXIST(4402, "父分类不存在"),
    CATEGORY_NAME_DUPLICATE(4403, "该分类名称已存在"),
    CATEGORY_PARENT_NOT_SELF(4404, "父分类不能选择自己"),
    CATEGORY_HAS_CHILD_NOT_DELETE(4405, "删除失败：该分类下存在子分类，请先删除子分类"),
    CATEGORY_HAS_SPU_NOT_DELETE(4406, "删除失败：该分类下存在商品数据，请先迁移/删除商品"),
    SPU_ERROR(4420, "商品异常"),
    SPU_NOT_EXIST(4421, "商品不存在"),
    SPU_NAME_DUPLICATE(4422, "商品名称已存在"),
    SPU_SKU_EMPTY(4423, "至少填写一条SKU规格"),


    CACHE_QUERY_EMPTY(4441, "缓存查询异常"),
    CACHE_LOCK_TIMEOUT(4442, "获取缓存锁超时，请稍后重试"),

    /**
     * 购物车 4500
     */
    CART_ERROR(4500, "购物车异常"),
    CART_NOT_EXIST(4501, "该商品购物车记录不存在，请检查后重试"),
    CART_EMPTY(4502, "购物车商品为空"),
    CART_NO_SELECT(4503, "商品购物车没有被选中的商品"),
    CART_LOAD(4504, "购物车加载中，请稍后重试"),

    /**
     * 订单 4600
     */
    ORDER_ERROR(4600, "订单异常"),
    ORDER_NOT_EXIST(4601, "订单不存在"),
    ORDER_CANCEL_FORBID(4602, "禁止取消订单"),
    ORDER_PAY_FORBID(4603, "订单非待支付状态，无法发起支付"),
    ORDER_NOT_SLEF_OPERATE_FORBID(4604, "禁止取消订单，该订单不是您创建的订单"),
    ORDER_NOT_SLEF_REFUND_OPERATE_FORBID(4605, "仅本人订单可退款"),
    ORDER_REFUND_OPERATE_FORBID(4606, "订单状态异常，禁止退款"),
    ORDER_REFUND_EXIST(4607, "订单已经退款，禁止重复退款"),

    /**
     * 库存 4700
     */
    STOCK_ERROR(4700, "库存异常"),
    // 4701 SKU库存记录不存在
    STOCK_NOT_EXIST(4701, "商品SKU库存记录不存在"),
    // 4702 可用库存不足（下单预占失败）
    STOCK_SHORTAGE(4702, "商品库存不足，无法下单"),
    // 4703 锁定库存不足（支付扣减失败）
    STOCK_LOCK_SHORT(4703, "订单锁定库存不足，支付扣减失败"),
    // 4704 库存操作乐观锁冲突/并发抢占失败
    STOCK_OPTIMISTIC_LOCK_FAIL(4704, "库存并发抢占失败，请重试"),
    // 4705 库存回滚失败（取消订单/退款）
    STOCK_ROLLBACK_FAIL(4705, "库存回滚操作失败"),
    // 4706 后台调整库存后可用库存为负
    STOCK_ADJUST_NEGATIVE(4706, "库存调整后可用库存不能为负数"),
    // 4707 库存流水记录保存失败
    STOCK_LOG_SAVE_FAIL(4707, "库存操作日志保存失败"),
    STOCK_SKU_LIST_EMPTY(4708, "商品规格数据缺失，删除库存记录失败"),


    /**
     * 支付 4800
     */
    PAY_ERROR(4800, "支付异常"),
    PAY_RECORD_NOT_EXIST(4801,  "支付记录不存在"),
    PAY_ORDER_NOT_EXIST(4810,  "支付时，订单不存在"),
    // 4802 支付回调重复请求
    PAY_REPEAT_CALLBACK(4802, "支付流水已处理，禁止重复回调"),
    // 4803 订单无商品明细，无法完成支付后扣库存
    PAY_ORDER_ITEM_EMPTY( 4803,  "订单缺少商品明细，无法扣减库存"),
    // 4804 支付成功后扣减库存失败
    PAY_STOCK_DEDUCT_FAIL( 4804,  "支付完成，但商品库存扣减失败"),
    // 4805 第三方渠道交易失败
    PAY_TRADE_FAIL( 4805,  "第三方支付渠道交易失败"),
    // 4806 支付类型不支持
    PAY_TYPE_UNSUPPORTED(4806, "当前不支持该支付方式"),
    // 4807 支付金额与订单实付金额不一致
    PAY_AMOUNT_MISMATCH( 4807,  "支付金额与订单应付金额不匹配"),
    // 4808 支付超时失效
    PAY_OVERTIME_INVALID(4808,  "支付单已超时失效，请重新发起支付"),
    // 4809 支付回调签名校验失败
    PAY_CALLBACK_SIGN_ERROR( 4809, "支付回调签名校验不通过，请求非法");

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