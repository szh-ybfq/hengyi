package com.zh.hengyi.component.rabbitmq.seckill;

import com.rabbitmq.client.Channel;
import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderMsgDTO;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.zh.hengyi.config.rabbitmq.SeckillRabbitConfig.SECKILL_ORDER_QUEUE;

@Component
@Slf4j
public class SeckillOrderConsumer {

    private final SeckillOrderService seckillOrderService;

    public SeckillOrderConsumer(SeckillOrderService seckillOrderService) {
        this.seckillOrderService = seckillOrderService;
    }

    @RabbitListener(queues = SECKILL_ORDER_QUEUE)
    public void consume(SeckillOrderMsgDTO msgDTO, Message message, Channel channel) throws IOException {
        try {
            // 全部业务逻辑交给service，消费者不写业务
            seckillOrderService.consumeSeckillOrder(msgDTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.error("秒杀消费异常 msg:{}",msgDTO,e);
            // 异常拒绝，重回队列，后续配置死信队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }
    }
}
