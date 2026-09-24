package com.zh.hengyi.config.sercurity.service;

import cn.hutool.core.collection.CollUtil;
import com.zh.hengyi.config.sercurity.permission.PermissionApi;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService {

    private final PermissionApi permissionApi;

    @Override
    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) { //...表示多字段java类型 = 数组
        // 特殊：跨租户访问
        /*if (skipPermissionCheck()) {
            return true;
        }*/

        // 权限校验
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        if (userId == null) {
            return false;
        }
        return permissionApi.hasAnyPermissions(userId, permissions);
    }

    @Override
    public boolean hasRole(String role) {
        return hasAnyRoles(role);
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        // 特殊：跨租户访问
        /*if (skipPermissionCheck()) {
            return true;
        }*/

        // 权限校验
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        if (userId == null) {
            return false;
        }
        return permissionApi.hasAnyRoles(userId, roles);
    }

//    @Override
//    public boolean hasScope(String scope) {
//        return hasAnyScopes(scope);
//    }
//
//    @Override
//    public boolean hasAnyScopes(String... scope) {
//        // 特殊：跨租户访问
//        if (skipPermissionCheck()) {
//            return true;
//        }
//
//        // 权限校验
//        LoginUser user = SecurityFrameworkUtils.getLoginUser();
//        if (user == null) {
//            return false;
//        }
//        return CollUtil.containsAny(user.getScopes(), Arrays.asList(scope));
//    }

}