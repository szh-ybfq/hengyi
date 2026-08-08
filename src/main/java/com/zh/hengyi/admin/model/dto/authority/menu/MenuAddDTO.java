package com.zh.hengyi.admin.model.dto.authority.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "菜单新增DTO")
public class MenuAddDTO {
    @Schema(description = "父id，0顶级")
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    @Schema(description = "前端路由path")
    private String path;
    @Schema(description = "组件component")
    private String component;
    @Schema(description = "权限标识")
    private String permission;
    @NotBlank(message = "菜单类型不能为空 M/C/F")
    private String menuType;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}