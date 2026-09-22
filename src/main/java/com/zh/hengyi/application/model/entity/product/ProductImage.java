package com.zh.hengyi.application.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.zh.hengyi.application.model.entity.BaseEntity;
import lombok.Data;

/**
 * 商品图片表
 * @TableName product_image
 */
@TableName(value ="product_image")
@Data
public class ProductImage extends BaseEntity {
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
     * 图片地址
     */
    private String imageUrl;

    /**
     * 图片排序
     */
    private Integer sort;

}