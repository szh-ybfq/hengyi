package com.zh.hengyi.common.constant;

public final class AuthConstant {

    /**
     * Redis 用户登录信息前缀
     */
    public static final String USER_PREFIX = "Login:user:";

    /**
     * Redis 用户登录会话信息前缀
     */
    public static final String TOKEN_PREFIX = "Login:user:";

    /**
     * 后台系统设备标识
     */
    public static final String DEVICE = "pc";

    /**
     * token 过期时间 24小时，单位：秒
     */
    public static final long EXPIRE_SECOND = 86400L;

    /**
     * 请求头 Authorization 名称
     */
    public static final String HEADER_TOKEN = "Authorization";

    /**
     * Bearer 前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    // 私有构造，禁止实例化
    private AuthConstant() {
    }
}
