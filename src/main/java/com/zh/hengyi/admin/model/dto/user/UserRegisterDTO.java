package com.zh.hengyi.admin.model.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "账号不能为空")
    @Size(min = 5, max = 60, message = "用户名长度必须在5~60位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 60, message = "密码长度必须在6~60位之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 50, message = "昵称长度必须在1~50位之间")
    private String nickname;

    private String avatar;
}