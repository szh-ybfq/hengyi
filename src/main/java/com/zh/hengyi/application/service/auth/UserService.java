package com.zh.hengyi.application.service.auth;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.application.model.dto.authority.user.*;
import com.zh.hengyi.application.model.vo.authority.user.UserFormVO;
import com.zh.hengyi.application.model.vo.authority.user.UserLoginVO;
import com.zh.hengyi.application.model.vo.authority.user.UserPageVO;
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
