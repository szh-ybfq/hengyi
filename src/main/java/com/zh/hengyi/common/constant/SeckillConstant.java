package com.zh.hengyi.common.constant;

public class SeckillConstant {
    // 秒杀活动状态 0未开始 1进行中 2已结束
    public static final Integer STATUS_NOT_START = 0;
    public static final Integer STATUS_RUNNING = 1;
    public static final Integer STATUS_FINISH = 2;

    // 秒杀商品状态
    public static final Integer SECKILL_GOODS_STATUS_NORMAL = 0;
    public static final Integer SECKILL_GOODS_STATUS_CLOSE = 1;

    // 订单类型
    public static final Integer ORDER_STATUS_NORMAL = 0; //普通订单
    public static final Integer ORDER_STATUS_SECKILL = 1; //秒杀订单

    // 秒杀已售
    public static final Integer GOODS_SOLD = 0;

    // redis key前缀
    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    public static final String SECKILL_USER_LIMIT_PREFIX = "seckill:user:limit:";
    public static final String SECKILL_USER_BUY_PREFIX = "seckill:user:buy:";
    // 分布式锁前缀
    public static final String SECKILL_LOCK_PREFIX = "lock:seckill:goods:";
}

