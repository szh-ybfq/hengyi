package com.zh.hengyi.admin.service.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
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

    /**
     * 用户提交秒杀请求：全部Redis层校验，通过发送MQ直接返回
     */
    void submitSeckillOrder(SeckillOrderCreateDTO dto);

    /**
     * MQ消费者调用：真正执行数据库下单逻辑，所有业务写在这里
     */
    void consumeSeckillOrder(SeckillOrderMsgDTO msgDTO);

    Order validSeckillOrderExist(Long seckilLId);

}

