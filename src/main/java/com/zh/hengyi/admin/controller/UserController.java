package com.zh.hengyi.admin.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.zh.hengyi.admin.model.dto.user.UserLoginDTO;
import com.zh.hengyi.admin.model.dto.user.UserRegisterDTO;
import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
import com.zh.hengyi.admin.service.UserService;
import com.zh.hengyi.common.result.Result;
import com.zh.hengyi.config.sercurity.login.LoginUser;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import com.zh.hengyi.config.sercurity.utils.jwt.JwtUtil;
import com.zh.hengyi.model.entity.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/v1")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterDTO register){
        userService.register(register);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO dto){
        UserLoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request){
        userService.logout(request);
        return Result.success();
    }

}
