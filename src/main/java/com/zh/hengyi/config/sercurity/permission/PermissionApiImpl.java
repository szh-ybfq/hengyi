package com.zh.hengyi.config.sercurity.permission;

public class PermissionApiImpl implements PermissionApi {


    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        return false;
    }


    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        return false;
    }
}
