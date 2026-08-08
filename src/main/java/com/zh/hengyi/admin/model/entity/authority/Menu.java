package com.zh.hengyi.admin.model.entity.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 菜单表
 * @TableName menu
 */
@TableName(value ="sys_menu")
@Data
public class Menu extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID，0代表顶级菜单
     */
    private Long parentId;

    /**
     * 菜单名称（页面显示名称）
     */
    private String menuName;

    /**
     * 前端路由地址 /system/user
     */
    private String path;

    /**
     * 前端vue组件路径 system/user/index
     */
    private String component;

    /**
     * 按钮权限标识 system:user:list（Security鉴权用）
     */
    private String permission;

    /**
     * 菜单类型 M目录 C菜单 F按钮
     */
    private String menuType;

    /**
     * element-plus图标名称
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否展示 0显示 1隐藏
     */
    private Integer visible;

}