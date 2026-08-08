package com.zh.hengyi.admin.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 商品SPU主表
 * @TableName product_spu
 */
@TableName(value ="product_spu")
@Data
public class ProductSpu extends BaseEntity {
    /**
     * spu主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品分类id
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String spuName;

    /**
     * 商品描述
     */
    private String spuDescription;

    /**
     * 销售参考价
     */
    private BigDecimal price;

    /**
     * 销量
     */
    private Integer saleCount;

    // status: 0下架 1上架

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}