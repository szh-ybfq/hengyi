package com.zh.hengyi.admin.service.authority.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.authority.RoleMapper;
import com.zh.hengyi.admin.mapper.authority.RoleMenuMapper;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAddDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAssignMenuDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleEditDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleQueryDTO;
import com.zh.hengyi.admin.model.entity.authority.Role;
import com.zh.hengyi.admin.model.entity.authority.RoleMenu;
import com.zh.hengyi.admin.model.vo.authority.role.RoleFormVO;
import com.zh.hengyi.admin.model.vo.authority.role.RoleOptionVO;
import com.zh.hengyi.admin.model.vo.authority.role.RolePageVO;
import com.zh.hengyi.admin.service.authority.RoleService;
import com.zh.hengyi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static com.zh.hengyi.common.result.ResultCode.ADMIN_ROLE_NOT_DELETE;
import static com.zh.hengyi.common.result.ResultCode.ROLE_NOT_EXIST;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public IPage<RolePageVO> getPage(RoleQueryDTO dto) {
        IPage<Role> rolePage = roleMapper.getPage(dto);
        return rolePage.convert(role -> BeanUtil.copyProperties(role, RolePageVO.class));
    }

    @Override
    public RoleFormVO getRoleInfo(Long id) {
        if (id == null) return null;
        return BeanUtil.copyProperties(baseMapper.selectById(id), RoleFormVO.class);
    }

    @Override
    public List<RoleOptionVO> getRoleOption() {
        return baseMapper.getRoleOption();
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        return roleMenuMapper.selectMenuIdByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(RoleAddDTO dto) {
        Role role = BeanUtil.copyProperties(dto,Role.class);
        baseMapper.insert(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(RoleEditDTO dto) {
        Role old = baseMapper.selectById(dto.getId());
        if(old == null){
            throw new BusinessException(ROLE_NOT_EXIST);
        }
        Role role = BeanUtil.copyProperties(dto,Role.class);
        baseMapper.updateById(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByIdCheck(Long id) {
        if(id.equals(1L)){
            throw new BusinessException(ADMIN_ROLE_NOT_DELETE);
        }
        baseMapper.deleteById(id);
        roleMenuMapper.deleteByRoleId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignMenu(RoleAssignMenuDTO dto) {
        Long roleId = dto.getRoleId();
        roleMenuMapper.deleteByRoleId(roleId);
        List<Long> menuIdList = dto.getMenuIdList();
        if(CollUtil.isNotEmpty(menuIdList)){
            for (Long menuId : menuIdList) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}