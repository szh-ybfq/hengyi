package com.zh.hengyi.config.sercurity.utils;

import com.zh.hengyi.config.sercurity.login.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}