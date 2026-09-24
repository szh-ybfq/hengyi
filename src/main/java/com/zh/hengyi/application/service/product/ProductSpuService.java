package com.zh.hengyi.application.service.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.application.model.dto.product.admin.ProductSkuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.application.model.dto.product.app.ProductSpuCardQueryDTO;
import com.zh.hengyi.application.model.entity.product.ProductSpu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuFormVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuPageVO;
import com.zh.hengyi.application.model.vo.product.app.ProductSpuPageCardVO;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【product_spu(商品SPU主表)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductSpuService extends IService<ProductSpu> {

    IPage<ProductSpuPageVO> getPageByAdmin(ProductSpuQueryDTO dto);

    IPage<ProductSpuPageCardVO> getPageByApp(ProductSpuCardQueryDTO dto);

    ProductSpuFormVO getSpuInfo(Long id);

    void add(ProductSpuAddDTO dto);

    void edit(ProductSpuEditDTO dto);

    void removeById(Long id);

    ProductSpu validSpuExist(Long id);

    void validSpuNameUnique(String spuName,Long id);
}
