package com.zh.hengyi.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.role.RoleAddDTO;
import com.zh.hengyi.admin.model.dto.role.RoleAssignMenuDTO;
import com.zh.hengyi.admin.model.dto.role.RoleEditDTO;
import com.zh.hengyi.admin.model.dto.role.RoleQueryDTO;
import com.zh.hengyi.admin.model.entity.Role;
import com.zh.hengyi.admin.model.vo.role.RoleFormVO;
import com.zh.hengyi.admin.model.vo.role.RoleOptionVO;
import com.zh.hengyi.admin.model.vo.role.RolePageVO;

import java.util.List;

public interface RoleService extends IService<Role> {
    IPage<RolePageVO> getPage(RoleQueryDTO dto);
    RoleFormVO getRoleInfo(Long id);
    List<RoleOptionVO> getRoleOption();
    List<Long> getMenuIdsByRoleId(Long roleId);
    void add(RoleAddDTO dto);
    void edit(RoleEditDTO dto);
    void removeByIdCheck(Long id);
    void assignMenu(RoleAssignMenuDTO dto);

}
