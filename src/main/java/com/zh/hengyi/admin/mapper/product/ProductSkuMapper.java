package com.zh.hengyi.admin.mapper.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    default List<ProductSku> selectListBySpuId(Long spuId){
        return selectList(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, spuId));
    }

}




