package com.zh.hengyi.admin.model.entity.authority;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 角色表
 * @TableName role
 */
@TableName(value ="sys_role")
@Data
public class Role extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识【重点：admin、normal，用于security权限判断】
     */
    private String roleKey;

    /**
     * 排序
     */
    private Integer sort;



}