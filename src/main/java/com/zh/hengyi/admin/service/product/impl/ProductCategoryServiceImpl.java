package com.zh.hengyi.admin.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import io.netty.util.internal.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    private final ProductSpuMapper productSpuMapper;

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