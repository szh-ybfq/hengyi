package com.zh.hengyi.admin.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductCategoryMapper;
import com.zh.hengyi.admin.mapper.product.ProductImageMapper;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.model.dto.product.ProductSkuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuEditDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.zh.hengyi.admin.model.entity.product.ProductImage;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.vo.product.ProductSkuFormVO;
import com.zh.hengyi.admin.model.vo.product.ProductSpuFormVO;
import com.zh.hengyi.admin.model.vo.product.ProductSpuPageVO;
import com.zh.hengyi.admin.service.product.ProductCategoryService;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    private final ProductCategoryService categoryService;
    private final ProductSkuMapper skuMapper;
    private final ProductSkuService skuService;
    private final ProductImageMapper imageMapper;

    @Override
    public IPage<ProductSpuPageVO> getPage(ProductSpuQueryDTO dto) {
        Page<ProductSpu> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<ProductSpu> spuPage = baseMapper.getPage(page, dto);
        return spuPage.convert(e -> BeanUtil.copyProperties(e, ProductSpuPageVO.class));
    }

    @Override
    public ProductSpuFormVO getSpuInfo(Long id) {
        ProductSpu spu = baseMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
        ProductSpuFormVO vo = BeanUtil.copyProperties(spu, ProductSpuFormVO.class);
        List<ProductSku> skuList = skuMapper.selectListBySpuId(id);
        vo.setSkuList(BeanUtil.copyToList(skuList, ProductSkuFormVO.class));
        // todo 查询图片组装imageUrlList
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProductSpuAddDTO dto) {
        validAdd(dto);
        ProductSpu spu = BeanUtil.copyProperties(dto, ProductSpu.class);
        baseMapper.insert(spu);
        Long spuId = spu.getId();
        // 批量插入SKU
        List<ProductSku> skuList = BeanUtil.copyToList(dto.getSkuList(), ProductSku.class);
        skuList.forEach(sku -> sku.setSpuId(spuId));
        skuService.saveBatch(skuList);
        // todo 批量插入商品图片
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProductSpuEditDTO dto) {
        validEdit(dto);
        ProductSpu spu = BeanUtil.copyProperties(dto, ProductSpu.class);
        baseMapper.updateById(spu);
        Long spuId = dto.getId();
        // 先删除旧SKU，再批量新增
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, spuId));
        List<ProductSku> skuList = BeanUtil.copyToList(dto.getSkuList(), ProductSku.class);
        skuList.forEach(sku -> sku.setSpuId(spuId));
        skuService.saveBatch(skuList);
        // todo 更新图片：先删旧，批量插入新图片
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        validSpuExist(id);
        baseMapper.deleteById(id);
        // 删除关联sku、图片
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, id));
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, id));
    }

    //  私有校验方法
    public void validAdd(ProductSpuAddDTO dto) {
        //校验商品分类存在
        categoryService.validCatogaryExist(dto.getCategoryId());
        // 校验spu名称重名
        ProductSpu exist = baseMapper.selectOneBySpuName(dto.getSpuName(), null);
        if (exist != null) {
            throw new BusinessException(ResultCode.SPU_NAME_DUPLICATE);
        }
        // 校验sku列表是否为空
        validSkuNotEmpty(dto.getSkuList());
    }

    public void validEdit(ProductSpuEditDTO dto) {
        //校验商品存在
        validSpuExist(dto.getId());
        //校验商品分类存在
        categoryService.validCatogaryExist(dto.getCategoryId());
        // 校验spu名称重名
        ProductSpu exist = baseMapper.selectOneBySpuName(dto.getSpuName(), dto.getId());
        if (exist != null) {
            throw new BusinessException(ResultCode.SPU_NAME_DUPLICATE);
        }
        // 校验sku列表是否为空
        validSkuNotEmpty(dto.getSkuList());
    }

    @Override
    public void validSpuExist(Long id) {
        ProductSpu db = baseMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
    }

    @Override
    public void validSkuNotEmpty(List<ProductSkuAddDTO> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            throw new BusinessException(ResultCode.SPU_SKU_EMPTY);
        }
    }
}