package com.zh.hengyi.component.rabbitmq.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.zh.hengyi.admin.mapper.order.OrderMqIdempotentMapper;
import com.zh.hengyi.admin.model.entity.order.OrderMqIdempotent;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.config.rabbitmq.OrderDelayMqConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OrderDelayConsumer {
    @Resource
    private OrderService orderService;

    @Resource
    private OrderMqIdempotentMapper orderMqIdempotentMapper;

    /**
     * 监听死信队列：执行订单超时关闭
     * 💎 正常发收：肯定是发送者发送消息后，消费者立刻或等待之后消费，不会等到过期，
     *    超时关闭： 这个是直接不消费，就干等到过期，让死信队列去执行
     */
    @RabbitListener(queues = OrderDelayMqConfig.ORDER_DLX_QUEUE)
    public void consume(Long orderId, Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        // 获取消息唯一ID
        String msgId = message.getMessageProperties().getMessageId();
        try {
            // 幂等判断：如果该消息已处理，直接ACK跳过
            boolean exist = orderMqIdempotentMapper.exists(new LambdaQueryWrapper<OrderMqIdempotent>().eq(OrderMqIdempotent::getMsgId, msgId));
            if (exist) {
                channel.basicAck(tag, false);
                log.info("消息{}已处理，直接跳过", msgId);
                return;
            }
            // 执行30min超时关闭
            orderService.closeOrderByTimeout(orderId);
            // 插入幂等记录（唯一索引，重复插入会报错）
            OrderMqIdempotent idempotent = OrderMqIdempotent.builder()
                    .msgId(msgId)
                    .businessId(orderId)
                    .build();
            orderMqIdempotentMapper.insert(idempotent);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("订单超时关单失败 orderId:{}", orderId, e);
            channel.basicNack(tag, false, true);
        }
    }
}
