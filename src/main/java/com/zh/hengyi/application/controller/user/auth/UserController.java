package com.zh.hengyi.application.controller.user.auth;


import com.zh.hengyi.application.model.dto.authority.user.*;
import com.zh.hengyi.application.model.vo.authority.user.UserFormVO;
import com.zh.hengyi.application.model.vo.authority.user.UserLoginVO;
import com.zh.hengyi.application.service.auth.UserService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理模块")
@RestController
@RequestMapping("/user/api/v1")
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
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request){
        userService.logout(request);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询用户信息")
    public Result<UserFormVO> getUserInfo(@PathVariable("id") Long id){
        return Result.success(userService.getUserInfo(id));
    }

}
