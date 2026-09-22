package com.zh.hengyi.application.service.seckill;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.application.model.dto.seckill.SeckillOrderCreateDTO;
import com.zh.hengyi.application.model.dto.seckill.SeckillOrderMsgDTO;
import com.zh.hengyi.application.model.entity.order.Order;
import com.zh.hengyi.application.model.entity.seckill.SeckillGoods;

public interface SeckillOrderService extends IService<SeckillGoods> {

    // 1.1 Redis+lua层拦截（削峰），发送MQ消息
    void submitSeckillOrder(Long seckillGoodsId,SeckillOrderCreateDTO dto);

    // 1.2 MQ消费（填谷），执行下秒杀单
    void consumeSeckillOrder(SeckillOrderMsgDTO msgDTO);

    void cancelSeckillOrder(Long orderId);

    void closeSeckillOrderTimeout(Long orderId);

//    // 4 用户退款
//    void applySeckillRefund(OrderRefundApplyDTO dto);

    Order validSeckillOrderExist(Long seckilLId);

}

