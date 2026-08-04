package com.zh.hengyi.admin.model.vo.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserFormVO implements Serializable {
    private String username;
    private String password;
    private String token;
}
