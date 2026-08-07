package com.zh.hengyi.config.sercurity.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zh.hengyi.admin.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LoginUser implements UserDetails, Serializable {

    private final User user;
    private final List<String> permissions;

    public LoginUser(User user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }


    @Override
    @JsonIgnore // 避免序列化SimpleGrantedAuthority集合 序列化、反序列化极易报错；引发循环引用等问题
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 权限封装
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JsonIgnore //必须实现getPassword()方法，但因只在登录校验时用，后续接口鉴权流程完全不需要密码,redis不能存明文密码，所以不用序列化
    public String getPassword() {
        return user.getPassword();
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    @JsonIgnore //均是标准方法 返回布尔值 存入 Redis 完全无用，忽略减少 JSON 体积、规避解析隐患。
    public boolean isAccountNonExpired() {return true;}

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {return true;}

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {return true;}

    @Override
    @JsonIgnore
    public boolean isEnabled() {return true;}
}