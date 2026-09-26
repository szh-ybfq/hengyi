package com.zh.hengyi.application.mapper.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuImageVO;
import com.zh.hengyi.common.enums.file.image.GoodsImageEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【product_image(商品图片表)】的数据库操作Mapper
* @createDate 2026-08-08 12:58:20
* @Entity com.zh.hengyi.admin.model.entity.product.ProductImage
*/
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    default ProductSpuImageVO getList(Long spuId){
        ProductSpuImageVO vo = new ProductSpuImageVO();
        List<ProductImage> productImages = selectList(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, spuId));

        vo.setMainImgList(productImages.stream()
                .filter(i->i.getImageType()== GoodsImageEnum.GOODS_MAIN.getType())
                .map(ProductImage::getImageUrl).toList());
        vo.setDetailImgList(productImages.stream()
                .filter(i->i.getImageType()== GoodsImageEnum.GOODS_DETAILS.getType())
                .map(ProductImage::getImageUrl).toList());
        vo.setParamImgList(productImages.stream()
                .filter(i->i.getImageType()== GoodsImageEnum.GOODS_PARAMS.getType())
                .map(ProductImage::getImageUrl).toList());
        return vo;
    };

}




