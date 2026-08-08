package com.zh.hengyi.admin.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "SPU分页VO")
public class ProductSpuPageVO {
    private Long id;
    private Long categoryId;
    private String spuName;
    private BigDecimal price;
    private Integer saleCount;
    private Integer status;
    private LocalDateTime createTime;
}