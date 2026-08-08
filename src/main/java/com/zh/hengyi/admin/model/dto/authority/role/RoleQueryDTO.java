package com.zh.hengyi.admin.model.dto.authority.role;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色查询条件")
public class RoleQueryDTO  extends BaseQueryDTO {
    private String roleName;
    private Integer status;
}