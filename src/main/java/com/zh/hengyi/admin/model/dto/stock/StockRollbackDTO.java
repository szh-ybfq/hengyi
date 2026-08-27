package com.zh.hengyi.admin.model.dto.stock;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 回滚库存DTO（订单取消/退款）
 */
@Data
public class StockRollbackDTO {
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotNull(message = "变更类型不能为空 3取消回补 4退款回补")
    private Integer changeType;
    private String remark;
    private List<SkuNumDTO> skuNumList;

    @Data
    public static class SkuNumDTO {
        private Long skuId;
        private Integer count;
    }
}
