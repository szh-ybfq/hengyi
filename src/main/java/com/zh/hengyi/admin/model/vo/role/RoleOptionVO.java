package com.zh.hengyi.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色下拉列表VO")
public class RoleOptionVO {
    private Long id;
    private String roleName;
}
