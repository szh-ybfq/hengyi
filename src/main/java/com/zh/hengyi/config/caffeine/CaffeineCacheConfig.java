package com.zh.hengyi.config.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zh.hengyi.common.utils.cache.product.ProductCacheUtils.CACHE_NAME;

@Configuration
public class CaffeineCacheConfig {

    /**
     * 商品分页本地缓存管理器
     * 最大缓存10000条分页数据，本地缓存5分钟过期，自动淘汰冷数据
     */
    @Bean("productLocalCacheManager")
    public CacheManager productLocalCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 写入5分钟后过期
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 最大缓存条目，防止堆内存溢出（JVM固定8G，限制本地缓存容量）
                .maximumSize(10000)
                // 缓存数据弱引用，GC自动回收
                .weakValues()
                // 开启缓存统计，监控命中率（压测优化关键）
                .recordStats()
        );

        // 缓存名称："product_page"
        cacheManager.setCacheNames(List.of(CACHE_NAME));
        return cacheManager;
    }
}