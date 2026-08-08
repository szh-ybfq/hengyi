package com.zh.hengyi.admin.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.zh.hengyi.admin.service.product.ProductCategoryService;
import com.zh.hengyi.admin.mapper.product.ProductCategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author HENGGE
* @description 针对表【product_category(商品分类表)】的数据库操作Service实现
* @createDate 2026-08-08 12:58:20
*/
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
    implements ProductCategoryService{

}




