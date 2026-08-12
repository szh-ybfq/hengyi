package com.zh.hengyi.admin.controller.authority;

import com.zh.hengyi.admin.model.dto.authority.menu.MenuAddDTO;
import com.zh.hengyi.admin.model.dto.authority.menu.MenuEditDTO;
import com.zh.hengyi.admin.model.dto.authority.menu.MenuQueryDTO;
import com.zh.hengyi.admin.model.vo.authority.menu.MenuFormVO;
import com.zh.hengyi.admin.service.authority.MenuService;
import com.zh.hengyi.admin.model.vo.authority.menu.MenuTreeVO;
import com.zh.hengyi.common.result.Result;
import com.zh.hengyi.config.sercurity.login.LoginUser;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/menu")
@Tag(name = "菜单管理模块")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @Operation(summary = "获取全部菜单")
    public Result<List<MenuTreeVO>> tree(MenuQueryDTO dto){
        return Result.success(menuService.getMenuTree(dto));
    }

    @GetMapping("/user/tree")
    @Operation(summary = "获取当前登录用户的菜单树【前端Sidebar侧边栏】")
    public Result<List<MenuTreeVO>> userTree(){
        return Result.success(menuService.getUserMenuTree(SecurityUtils.getLoginUser().getUser().getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id获取菜单信息")
    public Result<MenuFormVO> getMenuInfo(@PathVariable Long id){
        return Result.success(menuService.getMenuInfo(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增菜单")
    public Result<Void> add(@Valid @RequestBody MenuAddDTO dto){
        menuService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑菜单")
    public Result<Void> edit(@Valid @RequestBody MenuEditDTO dto){
        menuService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "递归删除菜单（连带所有子菜单）")
    public Result<Void> remove(@PathVariable Long id){
        menuService.removeByIdRecursive(id);
        return Result.success();
    }


}