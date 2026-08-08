package com.zh.hengyi.admin.service.authority;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAddDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAssignMenuDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleEditDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleQueryDTO;
import com.zh.hengyi.admin.model.entity.authority.Role;
import com.zh.hengyi.admin.model.vo.authority.role.RoleFormVO;
import com.zh.hengyi.admin.model.vo.authority.role.RoleOptionVO;
import com.zh.hengyi.admin.model.vo.authority.role.RolePageVO;

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
