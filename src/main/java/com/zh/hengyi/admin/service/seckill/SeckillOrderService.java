package com.zh.hengyi.admin.service.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderCreateDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderMsgDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogSeckillDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.seckill.SeckillActivity;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;

import java.util.List;

public interface SeckillOrderService extends IService<SeckillGoods> {

    // 1.1 用户提交秒杀请求：全部Redis层校验，发送MQ直接返回
    void submitSeckillOrder(SeckillOrderCreateDTO dto);

    // 1.2 MQ消费者调用：
    void consumeSeckillOrder(SeckillOrderMsgDTO msgDTO);

    // 2 取消订单
    void cancelSeckillOrder(Long orderId);

    // 3 30分钟超时关单
    void closeSeckillOrderTimeout(Long orderId);

//    // 4 用户退款
//    void applySeckillRefund(OrderRefundApplyDTO dto);


    Order validSeckillOrderExist(Long seckilLId);

}

