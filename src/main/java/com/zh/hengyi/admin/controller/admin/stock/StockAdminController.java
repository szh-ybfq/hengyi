package com.zh.hengyi.admin.controller.admin.stock;
import com.zh.hengyi.admin.model.dto.stock.StockEditDTO;
import com.zh.hengyi.admin.model.vo.stock.StockVO;
import com.zh.hengyi.admin.service.stock.StockService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/stock")
@Tag(name = "后台库存管理模块")
@RequiredArgsConstructor
public class StockAdminController {

    private final StockService stockService;

    @GetMapping("/{skuId}")
    @Operation(summary = "根据skuId查询库存")
    public Result<StockVO> getStock(@PathVariable Long skuId) {
        return Result.success(stockService.getStockBySkuId(skuId));
    }

    @GetMapping
    @Operation(summary = "批量查询库存")
    public Result<List<StockVO>> getStock(@RequestParam List<Long> skuIds) {
        return Result.success(stockService.getStockListBySkuIds(skuIds));
    }

    @PostMapping("/edit")
    @Operation(summary = "后台手动调整库存")
    public Result<Void> editStock(@Valid @RequestBody StockEditDTO dto) {
        stockService.editStock(dto);
        return Result.success();
    }
}
