package com.zh.hengyi.admin.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.model.entity.product.ProductParam;
import com.zh.hengyi.admin.service.product.ProductParamService;
import com.zh.hengyi.admin.mapper.product.ProductParamMapper;
import org.springframework.stereotype.Service;

/**
* @author HENGGE
* @description 针对表【product_param(商品参数表：描述「这个商品本身是什么」，是商品的公共属性，存的是这款商品通用、不变的属性)】的数据库操作Service实现
* @createDate 2026-08-08 12:58:20
*/
@Service
public class ProductParamServiceImpl extends ServiceImpl<ProductParamMapper, ProductParam> implements ProductParamService{

}




