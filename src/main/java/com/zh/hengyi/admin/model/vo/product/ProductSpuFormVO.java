package com.zh.hengyi.admin.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SPU表单回显VO")
public class ProductSpuFormVO {
    private Long id;
    private Long categoryId;
    private String spuName;
    private String spuDescription;
    private BigDecimal price;
    private Integer status;
    private List<ProductSkuFormVO> skuList;
//    private List<String> imageUrlList;
}