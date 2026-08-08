package com.zh.hengyi.admin.controller.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.product.ProductCategoryAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductCategoryEditDTO;
import com.zh.hengyi.admin.model.vo.product.ProductCategoryOptionVO;
import com.zh.hengyi.admin.model.vo.product.ProductCategoryTreeVO;
import com.zh.hengyi.admin.service.product.ProductCategoryService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/product/category")
@Tag(name = "商品分类管理模块")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "获取分类树形列表")
    public Result<List<ProductCategoryTreeVO>> getCategoryTree() {
        return Result.success(productCategoryService.getCategoryTree());
    }

    @GetMapping("/option")
    @Operation(summary = "获取分类下拉选项列表")
    public Result<List<ProductCategoryOptionVO>> getOptionList() {
        return Result.success(productCategoryService.getOptionList());
    }

    @PostMapping("/add")
    @Operation(summary = "新增商品分类")
    public Result<Void> add(@Valid @RequestBody ProductCategoryAddDTO dto) {
        productCategoryService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑商品分类")
    public Result<Void> edit(@Valid @RequestBody ProductCategoryEditDTO dto) {
        productCategoryService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "递归删除分类（校验子分类+商品引用）")
    public Result<Void> remove(@PathVariable Long id) {
        productCategoryService.removeByIdRecursive(id);
        return Result.success();
    }
}