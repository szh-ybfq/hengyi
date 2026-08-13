//package com.zh.hengyi.component.rabbitmq.consumer;
//
//import cn.hutool.core.util.StrUtil;
//import com.zh.hengyi.common.utils.cache.product.ProductCacheUtil;
//import com.zh.hengyi.config.rabbitmq.CacheDelayedMqConfig;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RedissonClient;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.cache.CacheManager;
//import org.springframework.stereotype.Component;
//
//import static com.zh.hengyi.common.utils.cache.product.ProductCacheUtil.CACHE_NAME;
//
//@Component
//@Slf4j
//public class CacheDelayDeleteConsumer {
//
//    @Resource
//    private RedissonClient redissonClient;
//
//    @Resource
//    private ProductCacheUtil productCacheUtil;
//
//    @Resource(name = "productLocalCacheManager")
//    private CacheManager caffeineCacheManager;
//
//    // 直接接收DTO，框架自动JSON反序列化为对象
//    @RabbitListener(queues = CacheDelayedMqConfig.CACHE_DELAY_QUEUE)
//    public void consumeDelayedDeleteMsg(CacheDelayMsgDTO msg) {
//        if (msg == null) {
//            log.warn("延迟删除消息为空，直接跳过");
//            return;
//        }
//        String cacheKey = msg.getCacheKey();
//        Long categoryId = msg.getCategoryId();
//        String type = msg.getType();
//
//        // 场景1：单key延迟删除
//        if (StrUtil.isNotBlank(cacheKey)) {
//            // 删除Redis二级缓存
//            redissonClient.getBucket(cacheKey).delete();
//            // 删除本地Caffeine一级缓存
//            caffeineCacheManager.getCache(CACHE_NAME).evict(cacheKey);
//            log.info("延迟删除单缓存key成功，key:{}", cacheKey);
//            return;
//        }
//
//        // 场景2：分类分页批量清理缓存
//        if ("categoryPage".equals(type) && categoryId != null) {
//            // 这里写你批量清理该分类下所有分页缓存的逻辑
//            productCacheUtil.clearCategoryPageCache(categoryId);
//            log.info("延迟清理分类分页缓存，categoryId:{}", categoryId);
//        }
//    }
//}
