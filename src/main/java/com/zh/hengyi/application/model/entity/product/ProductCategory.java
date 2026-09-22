package com.zh.hengyi.application.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.zh.hengyi.application.model.entity.BaseEntity;
import lombok.Data;

/**
 * 商品分类表
 * @TableName product_category
 */
@TableName(value ="product_category")
@Data
public class ProductCategory extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父分类id，0顶级分类
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 排序
     */
    private Integer sort;

}