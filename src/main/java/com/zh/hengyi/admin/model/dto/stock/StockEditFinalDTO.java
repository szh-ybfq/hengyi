package com.zh.hengyi.admin.model.dto.stock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 场景：前端新增商品sku时使用
@Data
public class StockEditFinalDTO {
    @NotNull(message = "skuId不能为空")
    private Long skuId;
    @NotNull(message = "最终库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer finalNum;
    private String remark;
}

