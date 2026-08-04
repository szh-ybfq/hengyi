package com.zh.hengyi.config.autofill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zh.hengyi.config.sercurity.login.LoginUser;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //获取当前登录用户ID，无登录返回null
    private Long getCurrentLoginUserId() {
        try {
            return SecurityUtils.getLoginUser().getUser().getId();
        } catch (Exception e) {
            // 无登录上下文场景：定时任务、初始化脚本、单元测试、异步线程
            return null;
        }
    }

    /**
     * 插入自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 时间
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 默认状态 0
        this.strictInsertFill(metaObject, "status", Integer.class, 0);
        // 逻辑删除默认0 未删除
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);

        // 当前登录用户ID
        Long loginUserId = getCurrentLoginUserId();
        if (loginUserId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, loginUserId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, loginUserId);
        }
    }


    /**
     * 更新自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 更新人
        Long loginUserId = getCurrentLoginUserId();
        if (loginUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, loginUserId);
        }
    }
}