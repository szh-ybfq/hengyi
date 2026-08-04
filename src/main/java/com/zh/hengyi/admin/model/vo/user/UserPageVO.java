package com.zh.hengyi.admin.model.vo.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPageVO implements Serializable {
    private String username;
    private String password;
    private String token;
}
