package com.zh.hengyi.admin.model.vo.authority.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "菜单树形VO，给前端Sidebar渲染侧边栏")
public class MenuTreeVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String permission;
    private String menuType;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;

    @Schema(description = "子菜单")
    private List<MenuTreeVO> children = new ArrayList<>();
}