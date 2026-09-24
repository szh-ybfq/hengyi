package com.zh.hengyi.application.controller.admin.goods;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuFormVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuPageVO;
import com.zh.hengyi.application.service.file.FileService;
import com.zh.hengyi.application.service.product.ProductImageService;
import com.zh.hengyi.application.service.product.ProductSpuService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/v1/product/spu")
@Tag(name = "后台商品管理模块")
@RequiredArgsConstructor
public class ProductSpuController {

    private final ProductSpuService spuService;
    private final ProductImageService productImageService;

    @GetMapping("/page")
    @Operation(summary = "SPU商品分页")   // @Validated 针对Get RequestParam校验
    public Result<IPage<ProductSpuPageVO>> getPage(@Validated ProductSpuQueryDTO dto) {
        return Result.success(spuService.getPageByAdmin(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个商品详情")
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

    /*    @PutMapping("/edit")
    @Operation(summary = "商品上下架")
    public Result<Void> edit(@Valid @RequestBody ProductSpuEditDTO dto) {
        spuService.edit(dto);
        return Result.success();
    }*/

    @DeleteMapping("/{id}")
    @Operation(summary = "删除SPU商品")
    public Result<Void> remove(@PathVariable Long id) {
        spuService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/image/delete")
    @Operation(summary = "删除文件")
    public Result<Void> remove(@RequestParam("fileUrl") String fileUrl) {
        productImageService.deleteByUrl(fileUrl);
        return Result.success();
    }
}