package com.zh.hengyi.admin.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SPU新增DTO")
public class ProductSpuAddDTO {

    @NotNull(message = "分类不能为空")
    @Schema(description = "所属分类id")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称")
    private String spuName;

    @NotNull(message = "参考售价不能为空")
    @Schema(description = "参考售价")
    private BigDecimal price;

    @Schema(description = "商品描述")
    private String spuDescription;

    @Schema(description = "上下架：0下架 1上架")
    private Integer status;

    @Schema(description = "sku规格集合，前端批量传")
    private List<ProductSkuAddDTO> skuList;

//    @Schema(description = "图片url集合")
//    private List<String> imageUrlList;
}