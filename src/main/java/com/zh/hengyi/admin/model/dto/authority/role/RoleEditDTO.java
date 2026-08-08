package com.zh.hengyi.admin.model.dto.authority.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "角色编辑DTO")
public class RoleEditDTO {
    @NotNull(message = "id不能为空")
    private Long id;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色标识不能为空")
    private String roleKey;
    private Integer sort;
    private Integer status;
}