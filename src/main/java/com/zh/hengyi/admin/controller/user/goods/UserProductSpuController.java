package com.zh.hengyi.admin.controller.user.goods;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.admin.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.dto.product.app.ProductSpuCardQueryDTO;
import com.zh.hengyi.admin.model.vo.product.admin.ProductCategoryOptionVO;
import com.zh.hengyi.admin.model.vo.product.admin.ProductCategoryTreeVO;
import com.zh.hengyi.admin.model.vo.product.admin.ProductSpuFormVO;
import com.zh.hengyi.admin.model.vo.product.admin.ProductSpuPageVO;
import com.zh.hengyi.admin.model.vo.product.app.ProductSpuPageCardVO;
import com.zh.hengyi.admin.service.product.ProductCategoryService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/api/v1/product/spu")
@Tag(name = "用户商品管理模块")
@RequiredArgsConstructor
public class UserProductSpuController {

    private final ProductSpuService spuService;
    private final ProductCategoryService productCategoryService;

    @GetMapping("/page")
    @Operation(summary = "获取商品分页")
    public Result<IPage<ProductSpuPageCardVO>> getProductSpuPage(@Validated  @ModelAttribute ProductSpuCardQueryDTO dto) {
        return Result.success(spuService.getPageByApp(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个商品详情")
    public Result<ProductSpuFormVO> getProductSpuInfo(@PathVariable Long id) {
        return Result.success(spuService.getSpuInfo(id));
    }

    @GetMapping("/option")
    @Operation(summary = "获取分类下拉选项列表")
    public Result<List<ProductCategoryOptionVO>> getCategoryOptionList() {
        return Result.success(productCategoryService.getOptionList());
    }
}