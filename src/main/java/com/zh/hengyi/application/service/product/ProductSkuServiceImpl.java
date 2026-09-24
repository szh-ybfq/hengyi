package com.zh.hengyi.application.service.product;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.product.ProductSkuMapper;
import com.zh.hengyi.application.model.entity.product.ProductSku;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    @Override
    public List<ProductSku> getBySpuId(Long spuId) {
        if (spuId == null) {
            return null;
        }
        return baseMapper.selectListBySpuId(spuId);
    }





    @Override
    public ProductSku validSkuExist(Long skuId) {
        ProductSku productSku = baseMapper.selectById(skuId);
        if (productSku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        return  productSku;
    }
}