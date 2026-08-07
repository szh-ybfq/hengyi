package com.zh.hengyi.admin.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "角色分配菜单DTO")
public class RoleAssignMenuDTO {
    @NotNull(message = "角色id不能为空")
    private Long roleId;
    @Schema(description = "菜单id集合")
    private List<Long> menuIdList;
}