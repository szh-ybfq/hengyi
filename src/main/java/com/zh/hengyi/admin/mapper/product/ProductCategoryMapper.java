package com.zh.hengyi.admin.mapper.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【product_category(商品分类表)】的数据库操作Mapper
* @createDate 2026-08-08 12:58:20
* @Entity com.zh.hengyi.admin.model.entity.product.ProductCategory
*/
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 根据分类名称查询（重名校验）
     */
    default ProductCategory selectOneByCategoryName(String categoryName, Long excludeId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getCategoryName, categoryName);
        if(excludeId != null){
            wrapper.ne(ProductCategory::getId, excludeId);
        }
        return selectOne(wrapper);
    }

}




