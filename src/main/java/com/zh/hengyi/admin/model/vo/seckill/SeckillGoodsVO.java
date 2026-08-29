package com.zh.hengyi.admin.model.vo.seckill;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillGoodsVO {
    private Long id;
    private Long activityId;
    private Long skuId;
    private String skuSpec;
    private String spuName;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer seckillSold;
    private Integer limitPerson;
    private Integer status;
}
