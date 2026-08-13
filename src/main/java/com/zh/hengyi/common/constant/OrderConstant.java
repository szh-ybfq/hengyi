package com.zh.hengyi.common.constant;

public final class OrderConstant {
    // 订单状态
    public static final Integer ORDER_NO_PAY = 0;        // 待支付
    public static final Integer ORDER_HAVING_PAY = 1;    // 已支付
    public static final Integer ORDER_HAVING_SEND = 2;  // 已发货
    public static final Integer ORDER_HAVING_DONE = 3;  // 已完成
    public static final Integer ORDER_HAVING_CANCEL = 4;// 已取消
    public static final Integer ORDER_DOING_REFUND = 5;// 退款中

    // 退款状态
    public static final Integer ORDER_REFUND_SUCCESS = 6;// 退款成功
    public static final Integer ORDER_REFUND_CANCEL = 7; // 拒绝退款

    // 私有构造禁止实例化
    private OrderConstant(){}
}
