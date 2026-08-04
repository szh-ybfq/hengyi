package com.zh.hengyi.config.sercurity.utils.jwt;


import com.zh.hengyi.config.sercurity.login.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static com.zh.hengyi.common.constant.AuthConstant.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

   /*
   * Jwt 认证过滤器：拦截所有进入后端http请求，统一token校验，不用每个controller都校验
   * 客户端请求 → Jwt 过滤器
        ├ 白名单路径 → 直接放行
        ├ 普通接口
        ├ 无 token /token 非法 / 过期 → 返回 401
        └ token 合法 → 解析用户信息存入上下文 → 放行进入 Controller
     完整流程：⭐⭐⭐
        1 请求头带上 Authorization: Bearer token
        2 进入你的 Jwt 过滤器
        3 解析 token → 获取 userId
        4 根据 userId 查询数据库得到用户信息
        5 手动构建 LoginUser，创建 Authentication 放入 Security 上下文
        6 Controller/service 层直接从上下文获取登录用户(id username password)
   * */
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException {
//        // 1 获取 token
//        String token = request.getHeader("token");
//        if (!StringUtils.hasText(token)) {
//            // 1.2 白名单，无token，直接放行（交给security判断是否需要登录）
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 可选：前端规范 Bearer token
//        if(token.startsWith("Bearer ")){
//            token = token.substring(7);
//        }
//
//        // 2 校验 token
//        if (!jwtUtil.verifyToken(token)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 3 根据id加载用户信息
//        User user = userMapper.selectById(jwtUtil.getUserId(token));
//        if(user == null){
//            // token有效但是用户不存在，直接放行前终止，抛出异常、返回未登录
//            filterChain.doFilter(request, response);
//            return;
//        }
//        // 4 设置上下文，手动构建 LoginUser、构建Authentication、放入Security上下文，用的时候直接拿
//        // SecurityContextHolder:⭐ 单次请求有效！请求结束自动清空
//        LoginUser loginUser = new LoginUser(user,null);//后期查询到权限后再放入
//        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
//                loginUser,
//                null, //credentials：密码 （登录成功后不再上下文存密码）
//                loginUser.getAuthorities() //authorities：权限集合
//        ));
//        filterChain.doFilter(request,response);
//    }

    // 构造器接收

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String token = getToken(request);

       if (token != null) { //无token，是白名单，直接放行（交给security判断是否需要登录）
           String userJson = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
           if (userJson == null) {
               // token失效/用户退出，401
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               return;
           }
           try {
               // json字符串转为LoginUser
               LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);
               // 设置认证信息
               UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                       loginUser, null, loginUser.getAuthorities());
               SecurityContextHolder.getContext().setAuthentication(authenticationToken);
           } catch (Exception e) {
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// json解析失败，拦截
               return;
           }
       }
       filterChain.doFilter(request, response);
   }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_TOKEN);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(7);
        }
        return null;
    }
}
