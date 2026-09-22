package com.zh.hengyi.application.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.product.ProductImageMapper;
import com.zh.hengyi.application.mapper.product.ProductSpuMapper;
import com.zh.hengyi.application.mapper.product.ProductSkuMapper;
import com.zh.hengyi.application.model.dto.product.admin.ProductSkuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.application.model.dto.product.app.ProductSpuCardQueryDTO;
//import com.zh.hengyi.admin.model.entity.product.EsProductDoc;
import com.zh.hengyi.application.model.entity.product.ProductImage;
import com.zh.hengyi.application.model.entity.product.ProductSku;
import com.zh.hengyi.application.model.entity.product.ProductSpu;
import com.zh.hengyi.application.model.entity.stock.Stock;
import com.zh.hengyi.application.model.vo.product.admin.ProductSkuFormVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuFormVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuPageVO;
import com.zh.hengyi.application.model.vo.product.app.ProductSpuPageCardVO;
import com.zh.hengyi.application.service.product.ProductCategoryService;
import com.zh.hengyi.application.service.product.ProductSkuService;
import com.zh.hengyi.application.service.product.ProductSpuService;
//import com.zh.hengyi.admin.service.product.es.EsProductSearchService;
import com.zh.hengyi.application.service.stock.StockService;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.cache.product.ProductCacheUtils;
import com.zh.hengyi.common.utils.convert.ConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    private final ProductCategoryService categoryService;
    private final ProductSkuMapper skuMapper;
    private final ProductSkuService skuService;
    private final ProductImageMapper imageMapper;
    private final ProductCacheUtils productCacheUtils;
    private final StockService stockService;
//    private final EsProductSearchService esProductSearchService;

    // 后台：商品分页
    @Override
    public IPage<ProductSpuPageVO> getPageByAdmin(ProductSpuQueryDTO dto) {

        // 0 粗粒度布隆过滤器拦截(只拦截不存在分类)
            //❗️必须放到一、二级缓存前，作为做前置校验，恶意不存在分类直接返回null
            //❗️️因为分页条件组合、商品名、上下架状态是无穷的，不可能把所有全写出来，分类可以穷尽，几百个，上下架状态兼顾前后端所以不预热
        RBloomFilter<Long> bloom = productCacheUtils.getProductBloom();
        Long categoryId = dto.getCategoryId();
        // 场景1：前端传了分类ID，但是布隆判定不存在 → 数据库一定无数据，直接返回空页，防缓存穿透
        if (productCacheUtils.bloomReady && categoryId != null && !bloom.contains(categoryId)) {
            return new Page<>(dto.getPageNum(), dto.getPageSize(),0);
        }
        // 场景2：前端没有传分类ID，不走布隆过滤器，正常查
        return  productCacheUtils.getTwoLevelCache(productCacheUtils.buildCacheKey(dto), ()->{
            Long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
            Long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
            IPage<ProductSpu> spuPage = baseMapper.getPageByAdmin(new Page<>(pageNum,pageSize), dto);
            return spuPage.convert(e -> BeanUtil.copyProperties(e, ProductSpuPageVO.class));
        });
    }

    // 前台：商品分页
    @Override
    public IPage<ProductSpuPageCardVO> getPageByApp(ProductSpuCardQueryDTO dto) {
//        // 1 查索引库
//        List<EsProductDoc> esList = esProductSearchService.searchProduct(dto.getSpuName(), dto.getCategoryId(), dto.getPageNum().intValue(), dto.getPageSize().intValue());
//
//        // 2.1 请求参数不为空，查索引库有文档
//        if (CollectionUtils.isNotEmpty(esList)) {
//            List<ProductSpuPageCardVO> voList = BeanUtil.copyToList(esList, ProductSpuPageCardVO.class);
//            Page<ProductSpuPageCardVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
//            page.setRecords(voList);
//            return page;
//        }

        // 2.2 索引库没有查到 或 请求参数为空，查库
        // 2.2.1 如果分类不为空，且预热完成，查布隆过滤器
        RBloomFilter<Long> bloom = productCacheUtils.getProductBloom();
        if (productCacheUtils.bloomReady && dto.getCategoryId() != null && !bloom.contains(dto.getCategoryId())) {
            return new Page<>(dto.getPageNum(), dto.getPageSize(),0);
        }
        // 2.2.2 否则查库
        return  productCacheUtils.getTwoLevelCache(productCacheUtils.buildCacheKeyByApp(dto), ()->{
            Long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
            Long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
            IPage<ProductSpu> spuPage = baseMapper.getPageByApp(new Page<>(pageNum,pageSize), dto);
            return spuPage.convert(e -> BeanUtil.copyProperties(e, ProductSpuPageCardVO.class));
        });
    }

    // 商品详情
    @Override
    public ProductSpuFormVO getSpuInfo(Long id) {
        // 1、校验 spu是否存在
        ProductSpu spu = validSpuExist(id);

        // 2、将 spu、skuList转换VO，再组装返回
        ProductSpuFormVO vo = BeanUtil.copyProperties(spu, ProductSpuFormVO.class);
        List<ProductSku> skus = skuMapper.selectListBySpuId(id);
        // 3、1 skus非空
        if (CollUtil.isNotEmpty(skus)) {
            List<ProductSkuFormVO> skuFormList = ConvertUtils.convertList(skus, ProductSkuFormVO.class);
                // 4、查可用库存，为skuListVO设置库存
            List<Long> skuIds = skuFormList.stream().map(ProductSkuFormVO::getId).collect(Collectors.toList());
            List<Stock> stockList = stockService.list(new LambdaQueryWrapper<Stock>().in(Stock::getSkuId, skuIds));
            for(int i=0;i<stockList.size();i++){
                skuFormList.get(i).setStock(stockList.get(i).getAvailableStock());
            }

            vo.setSkuList(skuFormList);
        }else {

            // 3、2 skus为空
            vo.setSkuList(new ArrayList<>());
        }
        return vo;
    }

    // 添加商品
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ProductSpuAddDTO dto) {
        validSaveData(BeanUtil.copyProperties(dto,ProductSpuEditDTO.class));

        ProductSpu spu = BeanUtil.copyProperties(dto, ProductSpu.class);
        // 1 插入spu
        baseMapper.insert(spu);

        // 2 批量插入sku
        List<ProductSku> skuList = BeanUtil.copyToList(dto.getSkuList(), ProductSku.class);
        skuList.forEach(sku -> sku.setSpuId( spu.getId()));//回填spuId
        skuService.saveBatch(skuList);

        // 3 批量入库,生成库存记录
        Map<Long,Integer> skuStockMap = new HashMap<>();
        // 只能for循环这样写，因为 skuList和 skuAddDTOList 没法一起forEach
        for(int i=0;i<skuList.size();i++){
            skuStockMap.put(skuList.get(i).getId(), dto.getSkuList().get(i).getStock());
        }
        stockService.batchCreateStock(skuStockMap);

        // 4 删除商品该分类下所有分页缓存
        productCacheUtils.clearCategoryPageCache(dto.getCategoryId());

        // 5、添加索引库
//        EsProductDoc doc = BeanUtil.copyProperties(spu, EsProductDoc.class);
//        esProductSearchService.saveDoc(doc);
    }

    // 修改商品
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(ProductSpuEditDTO dto) {
        validSaveData(dto);

        ProductSpu oldSpu = baseMapper.selectById(dto.getId());
        ProductSpu newSpu = BeanUtil.copyProperties(dto, ProductSpu.class);
        baseMapper.updateById(newSpu);

        // (1) 先删除旧sku、 旧库存记录
        Long spuId = dto.getId();
        //库存记录删除放前面，防止先删sku，导致库存参数为空
        stockService.batchLogicDeleteStock(skuMapper.selectSkuIdsBySpuId(spuId));
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, spuId));
        log.info("旧商品sku、 旧库存记录删除成功");

        // (2) 再批量新增sku、新库存记录
        List<ProductSku> skuList = BeanUtil.copyToList(dto.getSkuList(), ProductSku.class);
        skuList.forEach(sku -> sku.setSpuId(spuId));
        skuService.saveBatch(skuList);

        Map<Long,Integer> skuStockMap = new HashMap<>();
        for(int i=0;i<skuList.size();i++){
            skuStockMap.put(skuList.get(i).getId(), dto.getSkuList().get(i).getStock());
        }
        stockService.batchCreateStock(skuStockMap);


        // 💎💎 根据具体修改情况，采用不同延迟双删策略
        // 1. 判断是否修改分页展示字段：名称/价格/上下架
        boolean needClearCatePageCache = ObjUtil.notEqual(oldSpu.getSpuName(), newSpu.getSpuName())
                || newSpu.getPrice().compareTo(oldSpu.getPrice()) != 0
                || !oldSpu.getStatus().equals(newSpu.getStatus());

        // 2. 判断是否修改商品分类
        boolean changeCategory = !ObjUtil.equal(oldSpu.getCategoryId(), newSpu.getCategoryId());

        // 3. 待删除缓存key
        String cacheKey = productCacheUtils.buildCacheKey(BeanUtil.copyProperties(newSpu, ProductSpuQueryDTO.class));

        // 场景1：修改分类 最优先，新旧分类都要清分页缓存
        if (changeCategory) {
            // 清空旧分类分页缓存
            productCacheUtils.clearCategoryPageCache(oldSpu.getCategoryId());
            // 清空新分类分页缓存
            productCacheUtils.clearCategoryPageCache(newSpu.getCategoryId());
            log.info("清除新旧分类下分页缓存成功");
        } else if (needClearCatePageCache) {
            // 场景2：未换分类，但修改名称/价格/上下架，仅清当前分类分页缓存
            productCacheUtils.clearCategoryPageCache(newSpu.getCategoryId());
        }
            // 场景3：仅修改描述、sku等不影响分页的字段，不变动（(因为后台修改时根本不会传分页参数，系统根本找不到商品在哪个缓存key中)）

        // TODO【必做】任意商品修改、删除，都要清除商品详情缓存，商品新增可以不用

        // 4 修改索引库
//        EsProductDoc doc = BeanUtil.copyProperties(newSpu, EsProductDoc.class);
//        esProductSearchService.saveDoc(doc);
    }

    // 删除商品
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        ProductSpu spu = baseMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
        Long categoryId = spu.getCategoryId();

        // 1. 调用带事务的数据库删除方法
        doRemoveSpu(id);

        // 2. 事务提交完成后，再清理缓存（此时数据库数据已删除）
        productCacheUtils.clearCategoryPageCache(categoryId);

        // 3.删除索引库下该文档
//        esProductSearchService.deleteDoc(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doRemoveSpu(Long id) {
        baseMapper.deleteById(id);
        // 先删除库存记录，再删除sku列表
        stockService.batchLogicDeleteStock(skuMapper.selectSkuIdsBySpuId(id));
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, id));
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getSpuId, id));
    }





    private void validSaveData(ProductSpuEditDTO dto) {
        //校验商品存在
        if (dto.getId() != null) {
            validSpuExist(dto.getId());
        }

        //校验商品分类存在
        categoryService.validCatogaryExist(dto.getCategoryId());

        // 校验spu名称重名
        if (dto.getId() != null) {
            validSpuNameUnique(dto.getSpuName(), dto.getId());
        }else {
            validSpuNameUnique(dto.getSpuName(), null);
        }

        // 校验sku列表是否为空
        validSkuNotEmpty(dto.getSkuList());
    }
    @Override
    public ProductSpu validSpuExist(Long id) {
        ProductSpu spu = baseMapper.selectById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
        return spu;
    }
    @Override
    public void validSpuNameUnique(String spuName,Long id) {
        ProductSpu exist = baseMapper.selectOneBySpuName(spuName,id);
        if (exist != null) {
            throw new BusinessException(ResultCode.SPU_NAME_DUPLICATE);
        }
    }
    @Override
    public void validSkuNotEmpty(List<ProductSkuAddDTO> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            throw new BusinessException(ResultCode.SPU_SKU_EMPTY);
        }
    }
}