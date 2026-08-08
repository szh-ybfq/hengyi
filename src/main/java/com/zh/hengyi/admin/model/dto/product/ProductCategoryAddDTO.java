package com.zh.hengyi.admin.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商品分类新增DTO")
public class ProductCategoryAddDTO {

    @NotNull(message = "父分类ID不能为空")
    @Schema(description = "父分类id，0代表顶级", example = "0")
    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0启用 1禁用")
    private Integer status;
}