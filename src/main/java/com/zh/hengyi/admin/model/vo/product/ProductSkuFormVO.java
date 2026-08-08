package com.zh.hengyi.admin.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "SKU表单VO")
public class ProductSkuFormVO {
    private Long id;
    private Long spuId;
    private String skuSpec;
    private BigDecimal price;
    private Integer stock;
}