package com.zh.hengyi.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

//缓存延迟双删，解决读写并发脏缓存      （删redis-删mysql-删redis）
@Configuration
public class CacheDelayedMqConfig {
    // 延迟交换机、队列、路由key
    public static final String CACHE_DELAY_EXCHANGE = "cache_delay_exchange";
    public static final String CACHE_DELAY_QUEUE = "cache_delay_queue_delete";
    public static final String CACHE_DELAY_ROUTE_KEY = "cache.del.delete";

    // 创建延迟交换机
    @Bean
    public CustomExchange cacheDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        // 交换机连接延迟队列方式：直连
        args.put("x-delayed-type", "direct");
        //形参：交换机名称、交换机类型（选择延迟交换机）、交换机持久化（mq重启不丢失）、交换机自动删除（无人用也不删除）、额外参数
        return new CustomExchange(CACHE_DELAY_EXCHANGE,"x-delayed-message",true, false, args);
    }

    // 创建延迟队列
    @Bean
    public Queue cacheDelayQueue() {
        // 队列持久化（mq重启队列不丢失）
        return QueueBuilder.durable(CACHE_DELAY_QUEUE).build();
    }

    // 按照路由键绑定 延迟交换机和延迟队列
    @Bean
    public Binding cacheDelayBinding() {
        return BindingBuilder.bind(cacheDelayQueue())
                .to(cacheDelayExchange())
                .with(CACHE_DELAY_ROUTE_KEY)
                .noargs();
    }
}
