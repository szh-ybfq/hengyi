package com.zh.hengyi.component.rabbitmq.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.zh.hengyi.admin.mapper.order.OrderMqIdempotentMapper;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderMqIdempotent;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import com.zh.hengyi.common.constant.OrderConstant;
import com.zh.hengyi.config.rabbitmq.OrderDelayMqConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderDelayConsumer {
    @Resource
    private OrderService orderService;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private OrderMqIdempotentMapper orderMqIdempotentMapper;

    /**
     * 监听死信队列：执行订单超时关闭
     * 💎 正常发收：肯定是发送者发送消息后，消费者立刻或等待之后消费，不会等到过期，
     *    超时关闭： 这个是直接不消费，就干等到过期，让死信队列去执行
     */
    @RabbitListener(queues = OrderDelayMqConfig.ORDER_DLX_QUEUE)
    public void consume(Long orderId, Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        String msgId = message.getMessageProperties().getMessageId();
        try {
            // （一）“先查后改并发冲突覆盖”：如果先查询消息幂等表，再消费，可能会同时查到重复消费，所以直接修改，看结果
            OrderMqIdempotent idempotent = OrderMqIdempotent.builder()
                    .msgId(msgId)
                    .businessId(orderId)
                    .build();
            try {
                orderMqIdempotentMapper.insert(idempotent);
            } catch (DuplicateKeyException e) {
                // 唯一索引冲突：消息已经处理过，直接ack丢弃
                log.info("订单超时消息重复消费，跳过 msgId:{},orderId:{}", msgId, orderId);
                channel.basicAck(tag, false);
                return;
            }

            // 校验成功，执行超时关闭订单业务
            Order order = orderService.validOrderExist(orderId);
            if (order.getOrderType() == OrderConstant.ORDER_NORMAL) {
                // 普通订单
                orderService.closeOrderByTimeout(orderId);
            } else if (order.getOrderType() == OrderConstant.ORDER_SECKILL) {
                // 秒杀订单
                seckillOrderService.closeSeckillOrderTimeout(orderId);
            }
            // 业务执行成功，ack确认
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("订单超时关单失败 orderId:{}", orderId, e);
            // 异常重回队列重试
            channel.basicNack(tag, false, true);
        }
    }
}
