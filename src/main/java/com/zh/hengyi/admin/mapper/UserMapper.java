package com.zh.hengyi.admin.mapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.user.UserQueryDTO;
import com.zh.hengyi.admin.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    default User selectOneByUsername(String username){
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    default IPage<User> getPage(UserQueryDTO dto){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                // 1 为什么这么写？因为状态和前面模糊查询是并且关系，不是“或”关系，否则只满足状态即可，前面模糊查询失效
                // 2 StrUtil.isNotBlank很重要，如果未输入条件，就不拼接，更兼容
                .and(StrUtil.isNotBlank(dto.getUsername()) || StrUtil.isNotBlank(dto.getNickname()), w -> {
                    w.like(StrUtil.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername());
                    w.or(StrUtil.isNotBlank(dto.getNickname()));
                    w.like(StrUtil.isNotBlank(dto.getNickname()), User::getNickname, dto.getNickname());
                })
                .orderByAsc(User::getCreateTime);

        return selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }
}
