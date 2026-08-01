package com.zh.hengyi;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zh.hengyi.mapper")
public class HengyiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HengyiApplication.class, args);
    }

}
