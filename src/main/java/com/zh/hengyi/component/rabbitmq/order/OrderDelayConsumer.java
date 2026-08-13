package com.zh.hengyi.component.rabbitmq.order;

import com.rabbitmq.client.Channel;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.config.rabbitmq.OrderDelayMqConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 监听死信队列：执行订单超时关闭
     * 💎 正常发收：肯定是发送者发送消息后，消费者立刻或等待之后消费，不会等到过期，
     *    超时关闭： 这个是直接不消费，就干等到过期，让死信队列去执行
     */
    @RabbitListener(queues = OrderDelayMqConfig.ORDER_DLX_QUEUE)
    public void consume(Long orderId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("监听到订单超时消息，orderId:{}", orderId);
            // 执行超时关单逻辑
            orderService.closeOrderByTimeout(orderId);
            // 手动ACK，消息正常消费         multipe: 只确认当前这一条消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("订单超时关单失败 orderId:{}", orderId, e);
            // 消费失败，消息重回队列重试
            channel.basicNack(tag, false, true);
        }
    }
}
