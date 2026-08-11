package com.zh.hengyi;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HengyiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HengyiApplication.class, args);
    }

}
