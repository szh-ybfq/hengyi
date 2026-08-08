package com.zh.hengyi.admin.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import org.springframework.stereotype.Service;

/**
* @author HENGGE
* @description 针对表【product_sku(商品SKU库存表：描述「具体哪一个可卖的库存单元」，管库存、价格、规格、卖货。
库存保有单位，真正用来下单、扣库存、算钱的最小单元)】的数据库操作Service实现
* @createDate 2026-08-08 12:58:20
*/
@Service
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku>
    implements ProductSkuService{

}




