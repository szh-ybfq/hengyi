package com.zh.hengyi.component.rabbitmq.order;

import com.zh.hengyi.config.rabbitmq.OrderDelayMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderDelayProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单延迟消息（订单id）
     */
    public void sendOrderDelayMsg(Long orderId) {
        rabbitTemplate.convertAndSend(
                OrderDelayMqConfig.ORDER_DELAY_EXCHANGE,
                OrderDelayMqConfig.DELAY_ROUTING_KEY,
                orderId
        );
    }
}
