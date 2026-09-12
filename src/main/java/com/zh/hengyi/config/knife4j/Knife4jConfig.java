package com.zh.hengyi.config.knife4j;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("恒宜电商接口文档")
                        .version("1.0")
                        .description("欢迎您，尊贵的梅赛德斯‑奔驰车主\n" +
                                "愿我们的三叉星辉照亮您的事业和前程\uD83C\uDF38")
                        .contact(new Contact().name("zh"))
                );
    }
}

