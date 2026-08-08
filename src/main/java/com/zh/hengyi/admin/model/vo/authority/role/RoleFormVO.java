package com.zh.hengyi.admin.model.vo.authority.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色表单VO")
public class RoleFormVO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer status;
}