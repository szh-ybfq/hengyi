package com.zh.hengyi.admin.mapper.authority;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zh.hengyi.admin.model.entity.authority.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author HENGGE
* @description 针对表【role_menu(角色菜单关联表)】的数据库操作Mapper
* @createDate 2026-08-04 14:36:09
* @Entity com.zh.hengyi.model.entity.RoleMenu
*/
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    default void deleteByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
    }
    default List<Long> selectMenuIdByRoleId(Long roleId) {
        return  selectList(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)).stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
    }

}




