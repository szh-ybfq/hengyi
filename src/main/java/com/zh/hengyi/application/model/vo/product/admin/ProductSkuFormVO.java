package com.zh.hengyi.application.model.vo.product.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SKU表单VO")
public class ProductSkuFormVO {
    private Long id;
    private Long spuId;
    private String skuSpec;
    private BigDecimal price;
    private Integer stock;
    private String skuImgUrl;

}