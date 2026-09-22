package com.zh.hengyi.application.model.dto.stock;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 场景：后台管理库存时
@Data
public class StockEditDTO {
    @NotNull(message = "skuId不能为空")
    private Long skuId;

    @NotNull(message = "调整数量不能为空，正数加库存，负数减库存")
    private Integer adjustNum;

    private String remark;
}
