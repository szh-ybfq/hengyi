package com.zh.hengyi.admin.model.dto.seckill;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillGoodsAddDTO {
    private Long activityId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerson;
}