package com.zh.hengyi.admin.model.dto.stock;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

// 场景：后台管理库存时
@Data
public class StockEditDTO {
    @NotNull(message = "skuId不能为空")
    private Long skuId;

    @NotNull(message = "调整数量不能为空，正数加库存，负数减库存")
    private Integer adjustNum;

    private String remark;
}
