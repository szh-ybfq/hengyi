package com.zh.hengyi.admin.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 商品参数表：描述「这个商品本身是什么」，是商品的公共属性，存的是这款商品通用、不变的属性
 * @TableName product_param
 */
@TableName(value ="product_param")
@Data
public class ProductParam extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * spu主键
     */
    private Long spuId;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}