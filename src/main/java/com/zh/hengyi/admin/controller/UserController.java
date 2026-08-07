package com.zh.hengyi.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.user.*;
import com.zh.hengyi.admin.model.vo.user.UserFormVO;
import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
import com.zh.hengyi.admin.model.vo.user.UserPageVO;
import com.zh.hengyi.admin.service.UserService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理模块")
@RestController
@RequestMapping("/admin/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterDTO register){
        userService.register(register);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO dto){
        UserLoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request){
        userService.logout(request);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "用户分页查询")
    public Result<IPage<UserPageVO>> getPage(UserQueryDTO dto){
        return Result.success(userService.getPage(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询用户信息")
    public Result<UserFormVO> getUserInfo(@PathVariable("id") Long id){
        System.out.println(id);
        return Result.success(userService.getUserInfo(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    public Result<Void> add(@Valid @RequestBody UserAddDTO dto){
        userService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑用户")
    public Result<Void> edit(@Valid @RequestBody UserEditDTO dto){
        userService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> remove(@PathVariable Long id){
        userService.removeByIdCheck(id);
        return Result.success();
    }

    @PostMapping("/assignRole")
    @Operation(summary = "分配用户角色")
    public Result<Void> assignRole(@Valid @RequestBody UserAssignRoleDTO dto){
        userService.assignRole(dto);
        return Result.success();
    }
    @GetMapping("/roleIds/{userId}")
    @Operation(summary = "根据用户id查询已分配角色id集合")
    public Result<List<Long>> getRoleIdsByUserId(@PathVariable Long userId){
        return Result.success(userService.getRoleIdsByUserId(userId));
    }



}
