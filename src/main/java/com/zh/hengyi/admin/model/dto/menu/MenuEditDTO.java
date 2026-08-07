package com.zh.hengyi.admin.model.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "菜单编辑DTO")
public class MenuEditDTO {
    @NotNull(message = "菜单id不能为空")
    private Long id;
    private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    private String path;
    private String component;
    private String permission;
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}