package com.zh.hengyi.admin.mapper.authority;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.authority.role.RoleQueryDTO;
import com.zh.hengyi.admin.model.entity.authority.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.vo.authority.role.RoleOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【role(角色表)】的数据库操作Mapper
* @createDate 2026-08-04 14:36:09
* @Entity com.zh.hengyi.model.entity.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    default List<RoleOptionVO> getRoleOption(){
        List<Role> roles = selectList(null);
        return BeanUtil.copyToList(roles,RoleOptionVO.class);
    };

    default IPage<Role> getPage(RoleQueryDTO dto){
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>()
                .eq(dto.getStatus() != null, Role::getStatus, dto.getStatus())
                .and(StrUtil.isNotBlank(dto.getRoleName()), w -> {
                    w.like(Role::getRoleName, dto.getRoleName());
                })
                .orderByAsc(Role::getSort);

        return selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }
}




