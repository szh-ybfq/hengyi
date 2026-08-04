package com.zh.hengyi.admin.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

//用户分页查询请求参数
@Data
public class UserQueryDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 可选：验证码、记住我、设备标识
//    private String captcha;
//    private Boolean rememberMe;
}