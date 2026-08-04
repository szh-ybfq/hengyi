package com.zh.hengyi.config.sercurity.config;

import com.zh.hengyi.config.sercurity.utils.jwt.JwtAuthenticationFilter;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true) // 开启注解权限控制 @PreAuthorize
public class SecurityConfig {

    @Autowired
    private  JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭csrf，前后端分离JWT方案
                .csrf(csrf -> csrf.disable())
                // 不使用session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限配置
                .authorizeHttpRequests(auth -> auth // 定义 URL 访问权限规则
                        .requestMatchers(
                                "/admin/api/v1/login",
                                "/admin/api/v1/register")
                        .permitAll()                    //permitAll()：无条件放行，不需要登录、不需要 token
                        .anyRequest().authenticated()   //anyRequest()：剩下所有其他接口，authenticated()：必须完成认证（登录成功携带有效 token）才能访问
                ) // jwt过滤器放在账号密码过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 密码加密器（数据库密码必须BCrypt加密，和你之前Sa-Token加密保持一致）
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 认证管理器，登录接口调用
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
