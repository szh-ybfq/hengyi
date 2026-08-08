package com.zh.hengyi.admin.model.entity.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联表
 * @TableName role_menu
 */
@TableName(value ="sys_role_menu")
@Data
public class RoleMenu  {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long menuId;

}