package com.zh.hengyi.admin.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {

    @Override
    public List<ProductSku> getBySpuId(Long spuId) {
        return baseMapper.selectListBySpuId(spuId);
    }
}