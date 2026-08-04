package com.zh.hengyi.admin.service;

import com.zh.hengyi.admin.model.dto.user.UserLoginDTO;
import com.zh.hengyi.admin.model.dto.user.UserRegisterDTO;
import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void register(UserRegisterDTO register);

    UserLoginVO login(UserLoginDTO login);

    void logout(HttpServletRequest request);
}
