package com.zh.hengyi.admin.model.vo.authority.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色分页VO")
public class RolePageVO {
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}