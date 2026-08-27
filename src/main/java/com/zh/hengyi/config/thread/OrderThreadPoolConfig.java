package com.zh.hengyi.config.thread;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class OrderThreadPoolConfig {
    @Bean("orderTaskExecutor")
    public Executor orderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程
        executor.setCorePoolSize(10);
        // 最大线程
        executor.setMaxPoolSize(30);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程前缀
        executor.setThreadNamePrefix("order-task-");
        // 拒绝策略：调用者执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
