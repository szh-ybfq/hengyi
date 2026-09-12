package com.zh.hengyi.admin.model.vo.product.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户端首页商品卡片VO")
public class ProductSpuPageCardVO {
    private Long id;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer saleCount;

    @Schema(description = "商品主图地址")
    private String mainImageUrl;
}
