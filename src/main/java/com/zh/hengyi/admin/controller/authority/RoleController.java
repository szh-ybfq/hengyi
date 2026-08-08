package com.zh.hengyi.admin.controller.authority;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAddDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleAssignMenuDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleEditDTO;
import com.zh.hengyi.admin.model.dto.authority.role.RoleQueryDTO;
import com.zh.hengyi.admin.model.vo.authority.role.RoleFormVO;
import com.zh.hengyi.admin.model.vo.authority.role.RoleOptionVO;
import com.zh.hengyi.admin.model.vo.authority.role.RolePageVO;
import com.zh.hengyi.admin.service.authority.RoleService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/role")
@Tag(name = "角色管理模块")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @Operation(summary = "角色分页")
    public Result<IPage<RolePageVO>> getPage(RoleQueryDTO dto){
        return Result.success(roleService.getPage(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id获取角色")
    public Result<RoleFormVO> getRoleInfo(@PathVariable Long id){
        return Result.success(roleService.getRoleInfo(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取角色列表(下拉选项)")
    public Result<List<RoleOptionVO>> getRoleOption(){
        return Result.success(roleService.getRoleOption());
    }

    @PostMapping("/add")
    @Operation(summary = "新增角色")
    public Result<Void> add(@Valid @RequestBody RoleAddDTO dto){
        roleService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑角色")
    public Result<Void> edit(@Valid @RequestBody RoleEditDTO dto){
        roleService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> remove(@PathVariable Long id){
        roleService.removeByIdCheck(id);
        return Result.success();
    }

    @PostMapping("/assignMenu")
    @Operation(summary = "分配角色菜单")
    public Result<?> assignMenu(@RequestBody RoleAssignMenuDTO dto){
        roleService.assignMenu(dto);
        return Result.success();
    }

    @GetMapping("/menuIds/{roleId}")
    @Operation(summary = "根据角色id查询已分配的菜单id集合（回显分配菜单弹窗）")
    public Result<List<Long>> getMenuIdsByRoleId(@PathVariable Long roleId){
        return Result.success(roleService.getMenuIdsByRoleId(roleId));
    }

}