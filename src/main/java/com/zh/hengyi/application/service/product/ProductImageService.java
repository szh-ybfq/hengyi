package com.zh.hengyi.application.service.product;

import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuImageDTO;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuImageVO;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【product_image(商品图片表)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductImageService extends IService<ProductImage> {

    void batchSave(ProductSpuImageDTO dto);
    void batchUpadte(ProductSpuImageDTO dto);
    void validSpuImageExist(Long id);
    void validMainImagesNum(List<String> mainImgList);

    ProductSpuImageVO getList(Long id);

    void deleteByUrl(String fileUrl);
}
