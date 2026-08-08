package com.zh.hengyi.admin.service.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.product.ProductSkuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuEditDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.vo.product.ProductSpuFormVO;
import com.zh.hengyi.admin.model.vo.product.ProductSpuPageVO;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【product_spu(商品SPU主表)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductSpuService extends IService<ProductSpu> {

    IPage<ProductSpuPageVO> getPage(ProductSpuQueryDTO dto);

    ProductSpuFormVO getSpuInfo(Long id);

    void add(ProductSpuAddDTO dto);

    void edit(ProductSpuEditDTO dto);

    void removeById(Long id);

    void validSpuExist(Long id);

    void validSkuNotEmpty(List<ProductSkuAddDTO> skuList);
}
