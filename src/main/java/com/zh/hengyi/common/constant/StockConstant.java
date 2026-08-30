package com.zh.hengyi.common.constant;

public final class StockConstant {

    // 各类库存初始值
    public static final Integer LOCKED_STOCK = 0;
    public static final Integer SOLD_STOCK = 0;
    public static final Integer SECKILL_STOCK = 0;

    // 乐观锁初始值
    public static final Long VERSION = 0L;

    // 库存流水——变更类型
    public static final Integer CHANGE_TYPE_LOCK = 1; //下单锁定
    public static final Integer CHANGE_TYPE_DEDUCT = 2; //支付扣减
    public static final Integer CHANGE_TYPE_CANCEL_ROLLBACK = 3; //取消回补
    public static final Integer CHANGE_TYPE_REFUND_ROLLBACK = 4; //退款回补
    public static final Integer CHANGE_TYPE_ADMIN_EDIT = 5; //后台手动调整
    public static final Integer CHANGE_TYPE_SECKILL_DEDUCT = 6; //秒杀商品扣减库存
    public static final Integer CHANGE_TYPE_SECKILL_REVERT = 7; //秒杀商品归还库存
    public static final Integer CHANGE_TYPE_SECKILL_LOCK = 8; //秒杀下单锁定

    // 私有构造，禁止实例化
    private StockConstant() {

    }
}
