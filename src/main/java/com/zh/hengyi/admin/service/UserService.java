package com.zh.hengyi.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.user.*;
import com.zh.hengyi.admin.model.vo.user.UserFormVO;
import com.zh.hengyi.admin.model.vo.user.UserLoginVO;
import com.zh.hengyi.admin.model.vo.user.UserPageVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    void register(UserRegisterDTO register);

    UserLoginVO login(UserLoginDTO login);

    void logout(HttpServletRequest request);

    IPage<UserPageVO> getPage(UserQueryDTO dto);

    UserFormVO getUserInfo(Long id);

    List<Long> getRoleIdsByUserId(Long userId);

    void add(UserAddDTO dto);

    void edit(UserEditDTO dto);

    void removeByIdCheck(Long id);

    void assignRole(UserAssignRoleDTO dto);


}
