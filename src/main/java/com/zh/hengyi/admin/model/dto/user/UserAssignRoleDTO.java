package com.zh.hengyi.admin.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用户分配角色DTO")
public class UserAssignRoleDTO {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    @Schema(description = "角色id数组")
    private List<Long> roleIdList;
}