package com.zh.hengyi.config.redission;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
//    @Value("${spring.data.redis.password:}")
//    private String password;
    @Value("${spring.data.redis.database}")
    private int database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddr = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(redisAddr)
                .setDatabase(database);
                //.setPassword(password.isBlank() ? null : password);
        // 核心：全局统一使用字符串序列化，兼容你手动读写JSON字符串
        config.setCodec(StringCodec.INSTANCE);
        return Redisson.create(config);
    }
}