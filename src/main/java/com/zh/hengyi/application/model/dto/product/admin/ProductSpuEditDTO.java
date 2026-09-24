package com.zh.hengyi.application.model.dto.product.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SPU编辑DTO")
public class ProductSpuEditDTO {

    @NotNull(message = "spuId不能为空")
    @Schema(description = "spu主键id")
    private Long id;

    @NotNull(message = "分类id不能为空")
    @Schema(description = "所属分类id")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品描述")
    private String spuDescription;

    @NotNull(message = "参考售价不能为空")
    @Schema(description = "参考售价")
    private BigDecimal price;

    @NotNull(message = "商品状态不能为空")
    @Schema(description = "上下架：0下架 1上架")
    private Integer status;

    @NotEmpty(message = "商品规格集合不能为空")
    @Schema(description = "sku规格集合")
    private List<ProductSkuAddDTO> skuList;

    @NotEmpty(message = "商品主图不能为空")
    @Schema(description = "spu主图列表")
    private List<String> mainImgList;

    @NotEmpty(message = "商品详情图不能为空")
    @Schema(description = "spu详情图片集合")
    private List<String> detailImgList;

    @NotEmpty(message = "商品参数图不能为空")
    @Schema(description = "spu参数图片集合")
    private List<String> paramImgList;
}