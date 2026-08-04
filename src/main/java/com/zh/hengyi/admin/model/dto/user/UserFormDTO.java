package com.zh.hengyi.admin.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserFormDTO implements Serializable {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 可选：验证码、记住我、设备标识
//    private String captcha;
//    private Boolean rememberMe;
}