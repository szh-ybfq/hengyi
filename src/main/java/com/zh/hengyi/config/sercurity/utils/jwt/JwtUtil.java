package com.zh.hengyi.config.sercurity.utils.jwt;

import cn.hutool.core.util.StrUtil;
import com.zh.hengyi.common.constant.AuthConstant;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.zh.hengyi.common.constant.AuthConstant.BEARER_PREFIX;
import static com.zh.hengyi.common.constant.AuthConstant.HEADER_TOKEN;

@Component
public class JwtUtil {
    //yml配置密钥
    @Value("${jwt.secret}")
    private String secret;
    // 有效期 86400秒 = 1天
    @Value("${jwt.expire}")
    private long expire;

    // 获取密钥
    private SecretKey getSecretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    // 创建token
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSecretKey())
                .compact();
    }

    // 获取用户id
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    // 校验token
    public boolean verifyToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey()) // 传入签名密钥，解析必须使用相同密钥校验签名。
                    .build()
                    .parseClaimsJws(token); //解析校验token（是否正确、过期、格式、算法、为空）
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 提取token
    public static String extractToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        // 判断前缀 Bearer 空格 一共7位
        if (StrUtil.isNotBlank(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(7);
        }
        return null;
    }
}