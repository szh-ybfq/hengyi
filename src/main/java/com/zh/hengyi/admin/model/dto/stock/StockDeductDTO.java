package com.zh.hengyi.admin.model.dto.stock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 下单扣减库存DTO
 */
@Data
public class StockDeductDTO {
    private Long orderId;

    @NotNull(message = "订单号不能为空")
    private String orderSn;

    @NotEmpty(message = "商品sku列表不能为空(购买数量不能为空)")
    private List<SkuNumDTO> skuNumList;

    @Data
    public static class SkuNumDTO {
        @NotNull(message = "skuId不能为空")
        private Long skuId;
        @NotNull(message = "购买数量不能为空")
        private Integer count;
    }
}
