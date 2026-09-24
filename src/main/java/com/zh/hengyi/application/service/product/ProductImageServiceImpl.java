package com.zh.hengyi.application.service.product;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.product.ProductSpuMapper;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuImageDTO;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.zh.hengyi.application.mapper.product.ProductImageMapper;
import com.zh.hengyi.application.model.entity.product.ProductSpu;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuImageVO;
import com.zh.hengyi.application.service.file.FileService;
import com.zh.hengyi.common.enums.goods.GoodsImgEnum;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author HENGGE
* @description 针对表【product_image(商品图片表)】的数据库操作Service实现
* @createDate 2026-08-08 12:58:20
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {
    private final ProductSpuMapper productSpuMapper;
    private final FileService fileService;

    @Override
    public void batchSave(ProductSpuImageDTO dto) {
        log.info(dto.toString());

        // 1 业务校验：商品主图数量等于1
        validMainImagesNum(dto.getMainImgList());

        // 2 保存
        saveImages(dto);
    }

    @Override
    public void batchUpadte(ProductSpuImageDTO dto) {
        // 1 业务校验：商品主图数量等于1
        validMainImagesNum(dto.getMainImgList());

        // 2 校验旧图片集是否存在
        validSpuImageExist(dto.getId());

        // 3 删除旧图片集
        this.remove(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, dto.getId()));

        // 4 保存
        saveImages(dto);

    }

    @Override
    public ProductSpuImageVO getList(Long id) {
        return this.baseMapper.getList(id);
    }

    @Override
    public void deleteByUrl(String fileUrl) {
        // 1 删除oss上图片
        fileService.deleteImgByUrl(fileUrl);
        // 2 删除库表中图片
        this.baseMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getImageUrl,fileUrl));
    }

    // 校验：商品图片存在
    @Override
    public void validSpuImageExist(Long spuId) {
        List<ProductImage> productImages = baseMapper.selectList(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, spuId));
        if (CollUtil.isEmpty(productImages)) {
            throw new BusinessException(ResultCode.SPU_IMAGE_NOT_EXIST);
        }
    }

    // 校验：商品主图数量为1
    @Override
    public void validMainImagesNum(List<String> mainImgList) {
        if (mainImgList.size() > 1) { //非空校验已经在参数校验层完成
            throw new BusinessException(ResultCode.SPU_IMAGE_MAIN_NUM_OUTPUT);
        }
    }

    // 保存商品图片
    private void saveImages(ProductSpuImageDTO dto) {
        // 1 每张图片设置spuId、url
        // 不可直接new mainImages，否则无法放mainImgList对应图片的url，只能新建对象，最后转为列表
        List<ProductImage> mainImages = dto.getMainImgList().stream()
                .map(url -> {
                    ProductImage mainImage = new ProductImage();
                    mainImage.setSpuId(dto.getId());
                    mainImage.setImageType(GoodsImgEnum.MAIN_IMG.getType());
                    mainImage.setImageUrl(url);
                    return mainImage;
                })
                .collect(Collectors.toList());
        List<ProductImage> detailImages = dto.getDetailImgList().stream()
                .map(url -> {
                    ProductImage detailImage = new ProductImage();
                    detailImage.setSpuId(dto.getId());
                    detailImage.setImageType(GoodsImgEnum.DETAIL_IMG.getType());
                    detailImage.setImageUrl(url);
                    return detailImage;
                })
                .collect(Collectors.toList());
        List<ProductImage> paramImages = dto.getParamImgList().stream()
                .map(url -> {
                    ProductImage paramImage = new ProductImage();
                    paramImage.setSpuId(dto.getId());
                    paramImage.setImageType(GoodsImgEnum.PARAM_IMG.getType());
                    paramImage.setImageUrl(url);
                    return paramImage;
                })
                .collect(Collectors.toList());

        // 2 保存商品主图、商品详情图、商品参数图 （todo：只能上传单张，可拓展）
        this.saveBatch(mainImages);
        this.saveBatch(detailImages);
        this.saveBatch(paramImages);
    }
}




