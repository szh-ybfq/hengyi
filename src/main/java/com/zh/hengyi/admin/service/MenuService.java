package com.zh.hengyi.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.menu.MenuAddDTO;
import com.zh.hengyi.admin.model.dto.menu.MenuEditDTO;
import com.zh.hengyi.admin.model.dto.menu.MenuQueryDTO;
import com.zh.hengyi.admin.model.entity.Menu;
import com.zh.hengyi.admin.model.vo.menu.MenuTreeVO;
import com.zh.hengyi.admin.model.vo.menu.MenuFormVO;

import java.util.List;

public interface MenuService extends IService<Menu> {
    List<MenuTreeVO> getMenuTree(MenuQueryDTO dto);
    List<MenuTreeVO> getUserMenuTree(Long userId);
    MenuFormVO getMenuInfo(Long id);
    void add(MenuAddDTO dto);
    void edit(MenuEditDTO dto);
    void removeByIdRecursive(Long id);
    List<Long> getMenuIdsByRoleId(Long roleId);

}
