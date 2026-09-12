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

    // 本地消息表状态
    public static final Integer SECKILL_LOCAL_MSG_STATUS_NO = 0; // 0待发送
    public static final Integer SECKILL_LOCAL_MSG_STATUS_DOING = 1; // 1发送中，
    public static final Integer SECKILL_LOCAL_MSG_STATUS_HAVING = 2; // 2已发送，
    public static final Integer SECKILL_LOCAL_MSG_STATUS_FAIL = 3; // 3发送失败

    // 本地消息表重试次数
    public static final Integer SECKILL_LOCAL_MSG_RETRY_COUNT_DEFAULT = 0; //默认0
    public static final Integer SECKILL_LOCAL_MSG_RETRY_COUNT_MAX = 5; //最大5次

    // 本地消息表消费状态
    public static final Integer SECKILL_LOCAL_MSG_CONSUME_STATUS_NO = 0; // 0未消费
    public static final Integer SECKILL_LOCAL_MSG_CONSUME_STATUS_HAVING = 1; // 1消费成功
    public static final Integer SECKILL_LOCAL_MSG_CONSUME_STATUS_FAIL = 2; // 2消费失败

    // 秒杀已售
    public static final Integer GOODS_SOLD = 0;
    public static final Integer GOODS_LOCK = 0;

    // redis key前缀
    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    public static final String SECKILL_USER_LIMIT_PREFIX = "seckill:user:limit:";
    public static final String SECKILL_USER_BUY_PREFIX = "seckill:user:buy:";

    // 分布式锁前缀
    public static final String SECKILL_LOCK_PREFIX = "lock:seckill:goods:";
}

