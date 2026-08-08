package com.zh.hengyi.admin.controller.product;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.product.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuEditDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.vo.product.ProductSpuFormVO;
import com.zh.hengyi.admin.model.vo.product.ProductSpuPageVO;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/v1/product/spu")
@Tag(name = "商品SPU管理模块")
@RequiredArgsConstructor
public class ProductSpuController {

    private final ProductSpuService spuService;

    @GetMapping("/page")
    @Operation(summary = "SPU商品分页")
    public Result<IPage<ProductSpuPageVO>> getPage(ProductSpuQueryDTO dto) {
        return Result.success(spuService.getPage(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id获取SPU表单回显数据")
    public Result<ProductSpuFormVO> getInfo(@PathVariable Long id) {
        return Result.success(spuService.getSpuInfo(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增SPU商品")
    public Result<Void> add(@Valid @RequestBody ProductSpuAddDTO dto) {
        spuService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑SPU商品")
    public Result<Void> edit(@Valid @RequestBody ProductSpuEditDTO dto) {
        spuService.edit(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除SPU商品")
    public Result<Void> remove(@PathVariable Long id) {
        spuService.removeById(id);
        return Result.success();
    }
}