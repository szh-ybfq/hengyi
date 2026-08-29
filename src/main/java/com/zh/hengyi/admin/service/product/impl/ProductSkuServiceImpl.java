package com.zh.hengyi.admin.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.service.product.ProductSkuService;
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
    public void validSkuExist(Long skuId) {
        ProductSku productSku = baseMapper.selectById(skuId);
        if (productSku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
    }
}