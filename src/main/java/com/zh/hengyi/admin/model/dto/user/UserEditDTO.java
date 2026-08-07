package com.zh.hengyi.admin.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户编辑DTO")
public class UserEditDTO {
    @NotNull(message = "id不能为空")
    @Schema(description = "用户id")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 6,max = 60)
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1,max = 50)
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "状态0启用1禁用")
    private Integer status;
}