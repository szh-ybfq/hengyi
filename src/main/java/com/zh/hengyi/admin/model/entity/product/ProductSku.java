package com.zh.hengyi.admin.model.entity.product;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 商品SKU库存表：描述「具体哪一个可卖的库存单元」，管库存、价格、规格、卖货。
库存保有单位，真正用来下单、扣库存、算钱的最小单元
 * @TableName product_sku
 */
@TableName(value ="product_sku")
@Data
public class ProductSku extends BaseEntity{
    /**
     * sku主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联spu id
     */
    private Long spuId;

    /**
     * 规格json 如{"颜色":"黑","内存":"16G"}
     */
    private String skuSpec;

    /**
     * sku售价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 已售出库存
     */
    private Integer stockSold;

    /**
     * 乐观锁版本号，扣库存使用
     */
    @Version
    private Integer version;

}