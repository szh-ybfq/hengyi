//package com.zh.hengyi.component.rabbitmq.consumer;
//
//import cn.hutool.core.util.StrUtil;
//import com.zh.hengyi.config.rabbitmq.CacheDelayedMqConfig;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RedissonClient;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.cache.CacheManager;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//
//import static com.zh.hengyi.common.utils.cache.product.ProductCacheUtil.CACHE_NAME;
//import static com.zh.hengyi.common.utils.cache.product.ProductCacheUtil.MQ_MESSAGE_NAME;
//
//@Component
//@Slf4j
//public class CacheDelayDeleteConsumer {
//    @Resource
//    private RedissonClient redissonClient;
//    @Resource(name = "productLocalCacheManager")
//    private CacheManager caffeineCacheManager;
//
//    //消费者消费 延迟删除消息
//    @RabbitListener(queues = CacheDelayedMqConfig.CACHE_DELAY_QUEUE)
//    public void consumeDelayedDeleteMsg(Map<String, String> msg) {
//        String cacheKey = msg.get(MQ_MESSAGE_NAME);
//        if (StrUtil.isBlank(cacheKey)) return;
//
//        redissonClient.getBucket(cacheKey).delete();
//        caffeineCacheManager.getCache(CACHE_NAME).evict(cacheKey);
//        log.info("延迟删除一二级缓存成功");
//    }
//}
