package com.zh.hengyi.admin.mapper.authority;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.authority.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author HENGGE
* @description 针对表【user_role(用户角色关联)】的数据库操作Mapper
* @createDate 2026-08-04 14:36:09
* @Entity com.zh.hengyi.model.entity.UserRole
*/
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    default void deleteByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }
    default List<Long> selectRoleIdByUserId(Long userId) {
        return  selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)).stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }
}




