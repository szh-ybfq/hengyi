package com.zh.hengyi.admin.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "商品分类树形VO")
public class ProductCategoryTreeVO {
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "父id")
    private Long parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子节点集合")
    private List<ProductCategoryTreeVO> children;
}