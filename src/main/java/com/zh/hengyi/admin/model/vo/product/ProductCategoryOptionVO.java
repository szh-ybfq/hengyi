package com.zh.hengyi.admin.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类下拉选项VO")
public class ProductCategoryOptionVO {
    @Schema(description = "分类id")
    private Long id;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "父id")
    private Long parentId;
}