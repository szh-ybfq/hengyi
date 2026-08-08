package com.zh.hengyi.admin.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "SKU新增DTO（内嵌SPU）")
public class ProductSkuAddDTO {
    @NotNull(message = "规格json不能为空")
    @Schema(description = "规格json字符串")
    private String skuSpec;

    @NotNull(message = "sku售价不能为空")
    @Schema(description = "sku售价")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Schema(description = "库存数量")
    private Integer stock;
}