package com.zh.hengyi.application.service.product;

import com.zh.hengyi.application.model.dto.product.admin.ProductSpuImageDTO;
import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author HENGGE
* @description 针对表【product_image(商品图片表)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductImageService extends IService<ProductImage> {

    String uploadImage(Long spuId,MultipartFile file, String fileType);
    FileBatchUploadVO uploadImages(Long spuId,List<MultipartFile> fileList, String fileType);
    void batchSave(ProductSpuImageDTO dto);
    void batchUpadte(ProductSpuImageDTO dto);

    ProductSpuImageVO getImageList(Long spuId);
    List<String> getImageUrlBySpuId(Long spuId);
    Map<Long,String> getMainImageListBySpuIds(List<Long> spuIds);
    List<String> getImageUrlList();

    void deleteSingleImageByUrl(String imgUrl);
    void deleteBatchImagesByUrl(List<String> imgUrls);

    void validSpuImageExist(Long id);
    void validMainImagesNum(List<String> mainImgList);


}
