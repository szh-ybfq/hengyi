package com.zh.hengyi.admin.model.vo.order;

import lombok.Data;
import java.math.BigDecimal;

// 订单的每一项商品
@Data
public class OrderItemVO {
    private Long id;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuSpec;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
}