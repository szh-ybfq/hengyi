package com.zh.hengyi.application.model.vo.authority.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户表单VO")
public class UserFormVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
}