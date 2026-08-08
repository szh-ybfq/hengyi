package com.zh.hengyi.admin.mapper.product;

import com.zh.hengyi.admin.model.entity.product.ProductParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【product_param(商品参数表：描述「这个商品本身是什么」，是商品的公共属性，存的是这款商品通用、不变的属性)】的数据库操作Mapper
* @createDate 2026-08-08 12:58:20
* @Entity com.zh.hengyi.admin.model.entity.product.ProductParam
*/
@Mapper
public interface ProductParamMapper extends BaseMapper<ProductParam> {

}




