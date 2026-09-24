package com.zh.hengyi.application.mapper.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.application.model.dto.product.app.ProductSpuCardQueryDTO;
import com.zh.hengyi.application.model.entity.product.ProductSpu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.common.enums.goods.GoodsStatusEnum;
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
        return selectOne(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getSpuName, spuName)
                .ne(excludeId != null,ProductSpu::getId, excludeId)
        );
    }

    default IPage<ProductSpu> getPageByAdmin(Page<ProductSpu> page, ProductSpuQueryDTO dto){
        return selectPage(page,new LambdaQueryWrapper<ProductSpu>()
                .like(StrUtil.isNotBlank(dto.getSpuName()), ProductSpu::getSpuName, dto.getSpuName())
                .eq(dto.getCategoryId() != null, ProductSpu::getCategoryId, dto.getCategoryId())
                .eq(dto.getStatus() != null, ProductSpu::getStatus, dto.getStatus())
                .orderByDesc(ProductSpu::getCreateTime)
        );
    }

    default IPage<ProductSpu> getPageByApp(Page<ProductSpu> page, ProductSpuCardQueryDTO dto){
        return selectPage(page,new LambdaQueryWrapper<ProductSpu>()
                .like(StrUtil.isNotBlank(dto.getSpuName()), ProductSpu::getSpuName, dto.getSpuName())
                .eq(dto.getCategoryId() != null, ProductSpu::getCategoryId, dto.getCategoryId())
                .eq(ProductSpu::getStatus, GoodsStatusEnum.GOODS_UP.getStatus())
                .orderByDesc(ProductSpu::getCreateTime)
        );
    }
}



