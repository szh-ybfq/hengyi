package com.zh.hengyi.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.zh.hengyi.config.rabbitmq.OrderDelayMqConfig.ORDER_OUTIME_TTL;

@Configuration
public class SeckillRabbitConfig {

    public static final String SECKILL_DIRECT_EXCHANGE = "seckill.direct.exchange";
    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order.routing";

    @Bean
    public DirectExchange seckillDirectExchange(){
        //持久化交换机
        return ExchangeBuilder.directExchange(SECKILL_DIRECT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue seckillOrderQueue(){
        Map<String, Object> args = new HashMap<>();
        // 1 设置TTL 30min
        args.put("x-message-seckill-order-close-ttl",ORDER_OUTIME_TTL);
        //持久化队列
        return QueueBuilder.durable(SECKILL_ORDER_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding seckillBinding(DirectExchange seckillDirectExchange, Queue seckillOrderQueue){
        return BindingBuilder.bind(seckillOrderQueue)
                .to(seckillDirectExchange)
                .with(SECKILL_ORDER_ROUTING_KEY);
    }
}
