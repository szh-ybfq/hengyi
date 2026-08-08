package com.zh.hengyi.admin.model.dto.authority.menu;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单查询条件")
public class MenuQueryDTO extends BaseQueryDTO {
    private String menuName;
    private Integer status;
}