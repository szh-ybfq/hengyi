package com.zh.hengyi.admin.model.vo.authority.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserLoginVO implements Serializable {
    private Long id;
    private String username;
    private String token;// 登录凭证token
    private String nickname;
    private String avatar;// 头像地址
    private List<String> roleList;// 角色标识集合（如 ["admin"]）
    private List<String> permissionList;// 权限标识集合（如 ["system:user:list"]）
}
