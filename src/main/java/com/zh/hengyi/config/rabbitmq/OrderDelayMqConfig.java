package com.zh.hengyi.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderDelayMqConfig {
    // 1. 普通交换机
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    // 2. 死信交换机 （DLX Dead-Letter Exchange）
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    // 普通延迟队列（Time To Live 消息存活时间，30分钟过期）
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final Integer NORMAL_TTL = 30 * 60 * 1000; // ms
    /*
     * 正常消息无法被消费、过期、被拒绝，就会变成 死信；
     *     使用场景：消息过期（30分钟关闭订单），消费者主动拒绝消息
     *     实现：生产者 → TTL延迟队列 (30min 等待) → 消息过期 → DLX 死信交换机 → DLQ 死信队列 → 消费者真正执行关单
     * */
    // 死信队列（DLQ Dead-Letter Queue）
    public static final String ORDER_DLX_QUEUE = "order.dlx.queue";

    // 路由key
    public static final String DELAY_ROUTING_KEY = "order.delay";
    public static final String DLX_ROUTING_KEY = "order.dlx";

    // 延迟交换机
    @Bean
    public DirectExchange orderDelayExchange() {
        return ExchangeBuilder.directExchange(ORDER_DELAY_EXCHANGE).durable(true).build();
    }

    // 死信交换机
    @Bean
    public DirectExchange orderDlxExchange() {
        return ExchangeBuilder.directExchange(ORDER_DLX_EXCHANGE).durable(true).build();
    }

    // 延迟队列    绑定死信交换机
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 1 设置TTL 30min
        args.put("x-message-ttl",NORMAL_TTL);

        // 2 过期转发消息到死信交换机
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    // 死信队列
    @Bean
    public Queue orderDlxQueue() {
        return QueueBuilder.durable(ORDER_DLX_QUEUE).build();
    }

    // 延迟交换机绑定延迟队列
    @Bean
    public Binding delayBinding(Queue orderDelayQueue, DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderDelayQueue).to(orderDelayExchange).with(DELAY_ROUTING_KEY);
    }

    // 死信交换机绑定死信队列
    @Bean
    public Binding dlxBinding(Queue orderDlxQueue, DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderDlxQueue).to(orderDlxExchange).with(DLX_ROUTING_KEY);
    }
}
