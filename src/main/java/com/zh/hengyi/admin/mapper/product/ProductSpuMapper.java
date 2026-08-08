package com.zh.hengyi.admin.mapper.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【product_spu(商品SPU主表)】的数据库操作Mapper
* @createDate 2026-08-08 12:58:20
* @Entity com.zh.hengyi.admin.model.entity.product.ProductSpu
*/
@Mapper
public interface ProductSpuMapper extends BaseMapper<ProductSpu> {
     // 根据分类名称查询（重名校验）
    default ProductSpu selectOneBySpuName(String spuName, Long excludeId){
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpu::getSpuName, spuName);
        if(excludeId != null){
            wrapper.ne(ProductSpu::getId, excludeId);
        }
        return selectOne(wrapper);
    }

    default IPage<ProductSpu> getPage(Page<ProductSpu> page, ProductSpuQueryDTO dto){
        return selectPage(page,new LambdaQueryWrapper<ProductSpu>()
                .like(StrUtil.isNotBlank(dto.getSpuName()), ProductSpu::getSpuName, dto.getSpuName())
                .eq(dto.getCategoryId() != null, ProductSpu::getCategoryId, dto.getCategoryId())
                .eq(dto.getStatus() != null, ProductSpu::getStatus, dto.getStatus())
                .orderByDesc(ProductSpu::getCreateTime)
        );
    }
}



