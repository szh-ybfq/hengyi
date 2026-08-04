package com.zh.hengyi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User selectOneByUsername(String username){
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
