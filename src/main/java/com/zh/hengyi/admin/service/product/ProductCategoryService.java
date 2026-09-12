package com.zh.hengyi.admin.service.product;

import com.zh.hengyi.admin.model.dto.product.admin.ProductCategoryAddDTO;
import com.zh.hengyi.admin.model.dto.product.admin.ProductCategoryEditDTO;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.vo.product.admin.ProductCategoryOptionVO;
import com.zh.hengyi.admin.model.vo.product.admin.ProductCategoryTreeVO;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【product_category(商品分类表)】的数据库操作Service
* @createDate 2026-08-08 12:58:20
*/
public interface ProductCategoryService extends IService<ProductCategory> {

    List<ProductCategoryTreeVO> getCategoryTree();

    List<ProductCategoryOptionVO> getOptionList();

    void add(ProductCategoryAddDTO dto);

    void edit(ProductCategoryEditDTO dto);

    void removeByIdRecursive(Long id);

    /**
     * 校验当前分类是否存在
     */
    void validCatogaryExist(Long id);

    /**
     * 校验父分类是否存在
     */
    void validParentCatogaryExist(Long parentId);
}
