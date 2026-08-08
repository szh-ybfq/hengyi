package com.zh.hengyi.admin.controller.product;

import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/product/sku")
@Tag(name = "商品SKU管理模块")
@RequiredArgsConstructor
public class ProductSkuController {

    private final ProductSkuService skuService;

    @GetMapping("/list/{spuId}")
    @Operation(summary = "根据spuId查询sku列表")
    public Result<List<ProductSku>> getSkuList(@PathVariable Long spuId) {
        return Result.success(skuService.getBySpuId(spuId));
    }
}