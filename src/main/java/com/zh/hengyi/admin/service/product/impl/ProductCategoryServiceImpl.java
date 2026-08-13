package com.zh.hengyi.admin.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductCategoryMapper;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.model.dto.product.ProductCategoryAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductCategoryEditDTO;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.vo.product.ProductCategoryOptionVO;
import com.zh.hengyi.admin.model.vo.product.ProductCategoryTreeVO;
import com.zh.hengyi.admin.service.product.ProductCategoryService;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.cache.product.ProductCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author HENGGE
 * @description 针对表【product_category(商品分类表)】的数据库操作Service实现
 * @createDate 2026-08-08 12:58:20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    private final ProductSpuMapper productSpuMapper;
    private final ProductCacheUtils productCacheUtils;

    @Override
    public List<ProductCategoryTreeVO> getCategoryTree() {
        List<ProductCategory> allList = baseMapper.selectList(null);
        List<ProductCategoryTreeVO> voList = BeanUtil.copyToList(allList, ProductCategoryTreeVO.class);
        return buildTree(voList, 0L);
    }

    private List<ProductCategoryTreeVO> buildTree(List<ProductCategoryTreeVO> voAll, Long parentId) {
        List<ProductCategoryTreeVO> result = new ArrayList<>();
        for (ProductCategoryTreeVO vo : voAll) {
            if (Objects.equals(vo.getParentId(), parentId)) {
                List<ProductCategoryTreeVO> children = buildTree(voAll, vo.getId());
                vo.setChildren(children);
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<ProductCategoryOptionVO> getOptionList() {
        return BeanUtil.copyToList(baseMapper.selectList(null), ProductCategoryOptionVO.class);
    }

    @Override
    public void add(ProductCategoryAddDTO dto) {
        validAdd(dto);
        ProductCategory entity = BeanUtil.copyProperties(dto, ProductCategory.class);
        baseMapper.insert(entity);

        // 如果布隆还没预热完成：直接 return，不执行 add。
            // 作用：因为预热未完成，布隆过滤器还未初始化完成，会报错      就算暂时没添加，预热时也会将新增的分类添加到布隆过滤器，不会丢失
            // 因为新增分类 无非在查的时候或查完再，前者预热时添加到布隆，后者新增时添加到布隆
        if (!productCacheUtils.bloomReady) {
            log.warn("布隆未完成预热，跳过分类ID:{}", entity.getId());
            return;
        }
        productCacheUtils.getProductBloom().add(entity.getId());
        log.warn("新增商品分类id：{}，写入布隆过滤器", entity.getId());
    }

    @Override
    public void edit(ProductCategoryEditDTO dto) {
        validEdit(dto);
        ProductCategory entity = BeanUtil.copyProperties(dto, ProductCategory.class);
        baseMapper.updateById(entity);
    }

    @Override
    public void removeByIdRecursive(Long id) {
        validRemove(id);
        baseMapper.deleteById(id);

        // 清理该分类下所有类型缓存(目前只有商品分页缓存，只删除它)
        productCacheUtils.clearCategoryAllCache(id);
        log.info("清理该分类下所有类型缓存成功");
    }

    //  私有校验方法
    public void validAdd(ProductCategoryAddDTO dto) {
        // 校验父分类存在
        validParentCatogaryExist(dto.getParentId());
        // 分类名称不能重复
        ProductCategory exist = baseMapper.selectOneByCategoryName(dto.getCategoryName(), null);
        if (exist != null) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    public void validEdit(ProductCategoryEditDTO dto) {
        // 校验分类存在
        validCatogaryExist(dto.getId());
        // 校验父分类存在
        validParentCatogaryExist(dto.getParentId());
        // 不能把父id设置成自己，防止循环树
        if (Objects.equals(dto.getId(), dto.getParentId())) {
            throw new BusinessException(ResultCode.CATEGORY_PARENT_NOT_SELF);
        }
        // 重名校验，排除自己id
        ProductCategory exist = baseMapper.selectOneByCategoryName(dto.getCategoryName(), dto.getId());
        if (exist != null) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    public void validRemove(Long id) {
        validCatogaryExist(id);

        // 1. 判断是否存在子分类
        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<ProductCategory>().eq(ProductCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILD_NOT_DELETE);
        }

        // 2. 判断是否被SPU商品引用
        Long spuCount = productSpuMapper.selectCount(new LambdaQueryWrapper<ProductSpu>().eq(ProductSpu::getCategoryId, id));
        if (spuCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_SPU_NOT_DELETE);
        }
    }

    @Override
    public void validCatogaryExist(Long id) {
        ProductCategory productCategory = baseMapper.selectById(id);
        if (productCategory == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
    }

    @Override
    public void validParentCatogaryExist(Long parentId) {
        if (!Objects.equals(0L, parentId)) {
            ProductCategory parent = baseMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(ResultCode.CATEGORY_PARENT_NOT_EXIST);
            }
        }
    }

}