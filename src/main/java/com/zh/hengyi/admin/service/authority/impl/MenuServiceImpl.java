package com.zh.hengyi.admin.service.authority.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.authority.MenuMapper;
import com.zh.hengyi.admin.mapper.authority.RoleMenuMapper;
import com.zh.hengyi.admin.mapper.authority.UserRoleMapper;
import com.zh.hengyi.admin.model.dto.authority.menu.MenuAddDTO;
import com.zh.hengyi.admin.model.dto.authority.menu.MenuEditDTO;
import com.zh.hengyi.admin.model.dto.authority.menu.MenuQueryDTO;
import com.zh.hengyi.admin.model.entity.authority.Menu;
import com.zh.hengyi.admin.model.vo.authority.menu.MenuTreeVO;
import com.zh.hengyi.admin.model.vo.authority.menu.MenuFormVO;
import com.zh.hengyi.admin.service.authority.MenuService;
import com.zh.hengyi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.result.ResultCode.MENU_NOT_EXIST;
import static com.zh.hengyi.common.result.ResultCode.MENU_PARENT_NOT_SELF;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree(MenuQueryDTO dto) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(dto.getMenuName()),Menu::getMenuName,dto.getMenuName());
        wrapper.eq(dto.getStatus()!=null,Menu::getStatus,dto.getStatus());
        wrapper.orderByAsc(Menu::getSort);
        List<Menu> allList = baseMapper.selectList(wrapper);
        // 拷贝VO只执行一次
        List<MenuTreeVO> voList = BeanUtil.copyToList(allList, MenuTreeVO.class);
        return buildTree(voList,0L);
    }


    // 获取登录用户拥有的菜单树（给前端Sidebar侧边栏渲染）
    @Override
    public List<MenuTreeVO> getUserMenuTree(Long userId) {
        //1 用户→角色id
        List<Long> roleIds = userRoleMapper.selectRoleIdByUserId(userId);
        if(CollUtil.isEmpty(roleIds)){
            return Collections.emptyList();
        }
        //2 角色→菜单id
        Set<Long> menuIdSet = new HashSet<>();
        for (Long rid : roleIds) {
            List<Long> mids = roleMenuMapper.selectMenuIdByRoleId(rid);
            menuIdSet.addAll(mids);
        }
        if(CollUtil.isEmpty(menuIdSet)){
            return Collections.emptyList();
        }
        //3 查询菜单，过滤按钮F，只返回M目录、C菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId,menuIdSet);
        wrapper.ne(Menu::getMenuType,"F");
        wrapper.eq(Menu::getVisible,0);
        wrapper.eq(Menu::getStatus,0);
        wrapper.orderByAsc(Menu::getSort);
        List<Menu> menuList = baseMapper.selectList(wrapper);

        List<MenuTreeVO> voList = BeanUtil.copyToList(menuList, MenuTreeVO.class);
        System.out.println(buildTree(voList,0L));
        return buildTree(voList,0L);
    }

    @Override
    public MenuFormVO getMenuInfo(Long id) {
        if (id == null) {return null;}
        return BeanUtil.copyProperties(baseMapper.selectById(id),MenuFormVO.class);
    }

    /**
     * 修改入参：直接接收已经转换好的VO集合，不再接收Menu实体
     * 递归组装树形
     */
    private List<MenuTreeVO> buildTree(List<MenuTreeVO> voAll, Long parentId){
        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO vo : voAll) {
            if(Objects.equals(vo.getParentId(), parentId)){
                //递归找子节点，传入完整voAll
                List<MenuTreeVO> children = buildTree(voAll, vo.getId());
                vo.setChildren(children);
                result.add(vo);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(MenuAddDTO dto) {
        Menu menu = BeanUtil.copyProperties(dto,Menu.class);
        baseMapper.insert(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(MenuEditDTO dto) {
        Menu old = baseMapper.selectById(dto.getId());
        if(old == null){
            throw new BusinessException(MENU_NOT_EXIST);
        }
        // 不能把父id设置成自己id，循环嵌套
        if(Objects.equals(dto.getParentId(), dto.getId())){
            throw new BusinessException(MENU_PARENT_NOT_SELF);
        }
        Menu menu = BeanUtil.copyProperties(dto,Menu.class);
        baseMapper.updateById(menu);
    }

    /**
     * 递归删除：删除自己 + 所有后代子菜单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByIdRecursive(Long id) {
        List<Long> deleteIdList = new ArrayList<>();
        collectAllChildrenId(id,deleteIdList);
        deleteIdList.add(id);
        //批量删除
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId,deleteIdList);
        baseMapper.delete(wrapper);
    }
    /**
     * 递归收集所有子菜单id
     */
    private void collectAllChildrenId(Long parentId,List<Long> idList){
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId,parentId);
        List<Menu> children = baseMapper.selectList(wrapper);
        for (Menu m : children) {
            idList.add(m.getId());
            collectAllChildrenId(m.getId(),idList);
        }
    }
    /**
     * 递归向上查找所有父节点id，子菜单必须带上它所有上级目录
     * @param allMenus 全部可用菜单
     * @param childIds 用户拥有的子菜单id集合
     * @return 用户需要的全部id（子+全部父）
     */
    private Set<Long> findAllParentIds(List<Menu> allMenus, Set<Long> childIds){
        Map<Long, Menu> idMap = allMenus.stream().collect(Collectors.toMap(Menu::getId, x->x));
        Set<Long> result = new HashSet<>(childIds);
        for(Long id : childIds){
            Long pid = idMap.get(id).getParentId();
            while(pid != null && pid !=0){
                result.add(pid);
                Menu parent = idMap.get(pid);
                if(parent == null){
                    break;
                }
                pid = parent.getParentId();
            }
        }
        return result;
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectMenuIdByRoleId(roleId);
    }
}