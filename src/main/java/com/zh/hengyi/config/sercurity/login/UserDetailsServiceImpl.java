package com.zh.hengyi.config.sercurity.login;

import com.zh.hengyi.admin.mapper.authority.UserMapper;
import com.zh.hengyi.admin.model.entity.authority.User;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询数据库
        User user = userMapper.selectOneByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("用户名不存在");
        }
        // TODO 这里替换成你真实的权限集合，临时先给空集合
        return new LoginUser(user, Collections.emptyList());
    }
}
