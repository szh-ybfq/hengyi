package com.zh.hengyi.application.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.product.ProductSpuMapper;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuImageDTO;
import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.zh.hengyi.application.mapper.product.ProductImageMapper;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuImageVO;
import com.zh.hengyi.application.service.file.FileService;
import com.zh.hengyi.common.enums.file.image.GoodsImageEnum;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.enums.EnumConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    /**
     * 上传单个图片
     */
    @Override
    public String uploadImage(Long spuId,MultipartFile file, String fileType) {
        // 1 通用oss上传图片
        String fileUrl = fileService.uploadImage(file, fileType);

        // 2 校验上传云服务oss是否成功
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException(ResultCode.UPLOAD_IMAGE_OSS_ERROR);
        }

        // 3 上传到数据库(只在修改商品页面时使用，新增页面不使用，仅上传不提交关闭弹窗后会有oss垃圾，靠定时任务清理)
        if (!ObjUtil.isNull(spuId) ) {
            ProductImage productImage = new ProductImage();
            productImage.setSpuId(spuId);
            GoodsImageEnum goodsImageEnum = EnumConvertUtil.strToEnum(GoodsImageEnum.class, fileType);
            productImage.setImageType(goodsImageEnum.getType());
            productImage.setImageUrl(fileUrl);
            this.save(productImage);
        }
        return fileUrl;

    }

    /**
     * 批量上传图片
     */
    @Override
    public FileBatchUploadVO uploadImages(Long spuId,List<MultipartFile> fileList, String fileType) {
        // 1 通用oss批量上传图片
        FileBatchUploadVO fileBatchUploadVO = fileService.uploadImages(fileList, fileType);

        // 2 校验上传云服务oss是否成功
        if (CollUtil.isEmpty(fileBatchUploadVO.getSuccessUrlList())) {
            throw new BusinessException(ResultCode.UPLOAD_IMAGE_OSS_ERROR);
        }
        // 3 批量上传到数据库(只在修改商品页面时使用）
        // 太他妈的坑了，没想到传的spuId传的是“null” 不是null 醉了
        if (!ObjUtil.isNull(spuId)) {
            fileBatchUploadVO.getSuccessUrlList().forEach(url -> {
                ProductImage productImage = new ProductImage();
                productImage.setSpuId(spuId);
                GoodsImageEnum goodsImageEnum = EnumConvertUtil.strToEnum(GoodsImageEnum.class, fileType);
                productImage.setImageType(goodsImageEnum.getType());
                productImage.setImageUrl(url);
                this.save(productImage);
            });
        }

        return fileBatchUploadVO;
    }

    /**
     * 新增商品提交时批量保存图片
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchSave(ProductSpuImageDTO dto) {
        log.info(dto.toString());

        // 1 业务校验：商品主图数量等于1
        validMainImagesNum(dto.getMainImgList());

        // 2 保存
        saveImages(dto);
    }


    /**
     * 编辑商品提交时批量保存图片
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUpadte(ProductSpuImageDTO dto) {
        // 1 业务校验：商品主图数量等于1
        validMainImagesNum(dto.getMainImgList());

        // 2 校验旧图片集是否存在
        validSpuImageExist(dto.getId());

        // 3 批量删除图片
        this.remove(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, dto.getId()));

        // 4 保存
        saveImages(dto);

    }

    /**
     * 根据spuId获取商品图片列表
     */
    @Override
    public ProductSpuImageVO getImageList(Long spuId) {
        if (spuId == null) {
            throw new BusinessException(ResultCode.GET_IMAGE_SPU_NOT_EXIST);
        }
        return this.baseMapper.getList(spuId);
    }

    /**
     * 根据spuId获取商品图片url列表
     */
    @Override
    public List<String> getImageUrlBySpuId(Long spuId) {
        if (spuId == null) {
            throw new BusinessException(ResultCode.GET_IMAGE_SPU_NOT_EXIST);
        }
        return this.list(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, spuId)).stream().map(ProductImage::getImageUrl).toList();
    }

    /**
     * 批量查询spu对应的主图
     * @param spuIds
     * @return key:spuId value:主图url，无图片返回空字符串
     */
    @Override
    public Map<Long, String> getMainImageListBySpuIds(List<Long> spuIds) {
        // 参数校验
        if(CollUtil.isEmpty(spuIds)){
            return Collections.emptyMap();
        }

        List<ProductImage> list = this.list(new LambdaQueryWrapper<ProductImage>()
                .in(ProductImage::getSpuId, spuIds)
                .eq(ProductImage::getImageType, GoodsImageEnum.GOODS_MAIN.getType()));

        //先全部填充默认空字符串，健壮性，防止后续空指针异常
        Map<Long,String> resultMap = new HashMap<>();
        for(Long sid : spuIds){
            resultMap.put(sid,"");
        }
        //数据库查到覆盖值
        for(ProductImage img : list){
            resultMap.put(img.getSpuId(), img.getImageUrl());
        }
        return resultMap;
    }



    /**
     * 获取所有商品图片url列表
     */
    @Override
    public List<String> getImageUrlList() {
        return this.list().stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }

    /*
    * 先2后1 顺序不能对调，否则出现oss删除了，但是库中删除异常，事务回滚，这是前端现实404，因为库中残留旧url——>必须先1后2
    * 先1后2 假设oss失败了，oss可以回滚，但存在垃圾图片，因为库中已经删掉没有了——>定时任务解决
    *
    * 大坑：oss删除必须要解除异常，否则
    * */
    @Override
    public void deleteSingleImageByUrl(String imgUrl) {
        // 1 参数校验
        if (StrUtil.isBlank(imgUrl)) {
            throw new BusinessException(ResultCode.IMG_HTTP_NOT_EXIST);
        }

        // 2 删除库表中图片
        this.baseMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getImageUrl,imgUrl));
        // 3 删除oss图片
        fileService.deleteImgByUrl(imgUrl);
    }
    @Override
    public void deleteBatchImagesByUrl(List<String> imgUrls) {
        // 1 todo: 图片ids参数校验

        // 1 参数校验
        if (CollUtil.isEmpty(imgUrls)) {
            throw new BusinessException(ResultCode.IMG_HTTP_NOT_EXIST);
        }
        // 2 循环删除每张图片
        imgUrls.forEach(imgUrl -> {
           deleteSingleImageByUrl(imgUrl);
        });
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
                    mainImage.setImageType(GoodsImageEnum.GOODS_MAIN.getType());
                    mainImage.setImageUrl(url);
                    return mainImage;
                })
                .collect(Collectors.toList());
        List<ProductImage> detailImages = dto.getDetailImgList().stream()
                .map(url -> {
                    ProductImage detailImage = new ProductImage();
                    detailImage.setSpuId(dto.getId());
                    detailImage.setImageType(GoodsImageEnum.GOODS_DETAILS.getType());
                    detailImage.setImageUrl(url);
                    return detailImage;
                })
                .collect(Collectors.toList());
        List<ProductImage> paramImages = dto.getParamImgList().stream()
                .map(url -> {
                    ProductImage paramImage = new ProductImage();
                    paramImage.setSpuId(dto.getId());
                    paramImage.setImageType(GoodsImageEnum.GOODS_PARAMS.getType());
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




