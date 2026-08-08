package com.zh.hengyi.admin.model.vo.authority.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单表单VO")
public class MenuFormVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String menuType;
    private String icon;
    private Integer sort;
    private Integer status;
}