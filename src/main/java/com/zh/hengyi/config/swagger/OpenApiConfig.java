package com.zh.hengyi.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        // token名称，和前端请求头保持一致
        String authName = "BearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("输入token，格式：Bearer 空格 token值");

        return new OpenAPI()
                .info(new Info()
                        .title("后台管理系统接口文档")
                        .version("v1.0")
                        .description("RBAC权限后台接口"))
                .components(new Components().addSecuritySchemes(authName, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(authName));// 全部接口默认带上这个鉴权
    }
}