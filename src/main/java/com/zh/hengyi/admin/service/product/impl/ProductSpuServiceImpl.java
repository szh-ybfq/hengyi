package com.zh.hengyi.admin.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductImageMapper;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.model.dto.product.ProductSkuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuEditDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
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
import com.zh.hengyi.common.utils.cache.product.ProductCacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    private final ProductCategoryService categoryService;
    private final ProductSkuMapper skuMapper;
    private final ProductSkuService skuService;
    private final ProductImageMapper imageMapper;
    private final ProductCacheUtil productCacheUtil;

    // 商品分页高并发接口优化
    @Override
    public IPage<ProductSpuPageVO> getPage(ProductSpuQueryDTO dto) {
        //IPage<ProductSpu> spuPage = baseMapper.getPage(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
        //return spuPage.convert(e -> BeanUtil.copyProperties(e, ProductSpuPageVO.class));

        // 0 粗粒度布隆过滤器拦截(只拦截不存在分类)
        //❗️必须放到一、二级缓存前，作为做前置校验，恶意不存在分类直接返回null
        //❗️️因为分页条件组合、商品名、上下架状态是无穷的，不可能把所有全写出来，分类可以穷尽，几百个，上下架状态兼顾前后端所以不预热
        RBloomFilter<Long> bloom = productCacheUtil.getProductBloom();
        Long categoryId = dto.getCategoryId();
        // 场景1：前端传了分类ID，但是布隆判定不存在 → 数据库一定无数据，直接返回空页，防缓存穿透
        // 场景2：前端没有传分类ID，不走布隆过滤器，正常查
        if (productCacheUtil.bloomReady && categoryId != null && !bloom.contains(categoryId)) {
            return new Page<>(dto.getPageNum(), dto.getPageSize(),0);
        }

        String cacheKey = productCacheUtil.buildCacheKey(dto);

        return  productCacheUtil.getTwoLevelCache(cacheKey, ()->{
            IPage<ProductSpu> spuPage = baseMapper.getPage(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
            return spuPage.convert(e -> BeanUtil.copyProperties(e, ProductSpuPageVO.class));
        });



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

        // 删除商品该分类下所有分页缓存
        productCacheUtil.clearCategoryPageCache(dto.getCategoryId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProductSpuEditDTO dto) {
        validEdit(dto);
        ProductSpu oldSpu = baseMapper.selectById(dto.getId());
        ProductSpu newSpu = BeanUtil.copyProperties(dto, ProductSpu.class);
        baseMapper.updateById(newSpu);

        // 先删除旧SKU，再批量新增sku
        Long spuId = dto.getId();
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, spuId));
        List<ProductSku> skuList = BeanUtil.copyToList(dto.getSkuList(), ProductSku.class);
        skuList.forEach(sku -> sku.setSpuId(spuId));
        skuService.saveBatch(skuList);


        // 💎💎 根据具体修改情况，采用不同延迟双删策略
        // 1. 判断是否修改分页展示字段：名称/价格/上下架
        boolean needClearCatePageCache = ObjUtil.notEqual(oldSpu.getSpuName(), newSpu.getSpuName())
                || newSpu.getPrice().compareTo(oldSpu.getPrice()) != 0
                || !oldSpu.getStatus().equals(newSpu.getStatus());

        // 2. 判断是否修改商品分类
        boolean changeCategory = !ObjUtil.equal(oldSpu.getCategoryId(), newSpu.getCategoryId());

        // 3. 待删除缓存key
        String cacheKey = productCacheUtil.buildCacheKey(BeanUtil.copyProperties(newSpu, ProductSpuQueryDTO.class));

        // 场景1：修改分类 最优先，新旧分类都要清分页缓存
        if (changeCategory) {
            // 清空旧分类分页缓存
            productCacheUtil.clearCategoryPageCache(oldSpu.getCategoryId());
            // 清空新分类分页缓存
            productCacheUtil.clearCategoryPageCache(newSpu.getCategoryId());
            log.info("清除新旧分类下分页缓存成功");
        } else if (needClearCatePageCache) {
            // 场景2：未换分类，但修改名称/价格/上下架，仅清当前分类分页缓存
            productCacheUtil.clearCategoryPageCache(newSpu.getCategoryId());
        }
            // 场景3：仅修改描述、sku等不影响分页的字段，不变动（(因为后台修改时根本不会传分页参数，系统根本找不到商品在哪个缓存key中)）

        // TODO【必做】任意商品修改、删除，都要清除商品详情缓存，商品新增可以不用
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        ProductSpu spu = baseMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
        Long categoryId = spu.getCategoryId();

        // 2. 调用带事务的数据库删除方法
        doRemoveSpu(id);

        // 1. 事务提交完成后，再清理缓存（此时数据库数据已删除）
        productCacheUtil.clearCategoryPageCache(categoryId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doRemoveSpu(Long id) {
        baseMapper.deleteById(id);
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