package com.zh.hengyi.common.constant;

public final class PayConstant {

    // 支付类型
    public static final Integer PAY_TYPE_WECHAT = 1; //微信
    public static final Integer PAY_TYPE_ZHIFUBAO = 2; //支付宝
    public static final Integer PAY_TYPE_TEST = 3; //模拟测试

    // 支付状态
    public static final Integer PAY_NO = 0; //待支付
    public static final Integer PAY_SUCCESS = 1; //支付成功
    public static final Integer PAY_FAIL = 2; //支付失败
    public static final Integer PAY_HAVING_REFUND = 3; //已退款

    // 私有构造，禁止实例化
    private PayConstant() {

    }
}
