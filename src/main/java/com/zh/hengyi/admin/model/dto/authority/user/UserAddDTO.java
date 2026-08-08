package com.zh.hengyi.admin.model.dto.authority.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户新增DTO")
public class UserAddDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 6,max = 60,message = "用户名6‑60位")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 60,message = "密码6‑60位")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1,max = 50,message = "昵称1‑50位")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "状态0启用1禁用")
    private Integer status;
}
