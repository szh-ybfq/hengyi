package com.zh.hengyi.admin.model.dto.authority.user;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户分页查询条件")
public class UserQueryDTO extends BaseQueryDTO {
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "状态")
    private Integer status;
}