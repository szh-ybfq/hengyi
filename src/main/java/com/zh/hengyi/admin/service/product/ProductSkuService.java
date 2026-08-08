package com.zh.hengyi.admin.service.product;

import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author HENGGE
* @description 针对表【product_sku(商品SKU库存表：描述「具体哪一个可卖的库存单元」，管库存、价格、规格、卖货。
库存保有单位，真正用来下单、扣库存、算钱的最小单元)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductSkuService extends IService<ProductSku> {

}
