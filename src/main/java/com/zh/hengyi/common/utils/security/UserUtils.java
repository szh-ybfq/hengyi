package com.zh.hengyi.common.utils.security;

import com.zh.hengyi.admin.model.entity.authority.User;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserUtils {
    // 登录校验,返回登录用户对象
    public static User validUserLogin(){
        User user = SecurityUtils.getLoginUser().getUser();
        if (user == null){
            throw new BusinessException(ResultCode.LOGIN_NOT_EXIST);
        }
        return user;
    };
}
