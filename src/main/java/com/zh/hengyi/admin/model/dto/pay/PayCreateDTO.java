package com.zh.hengyi.admin.model.dto.pay;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayCreateDTO {
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotNull(message = "支付方式不能为空 1微信 2支付宝 3测试支付")
    private Integer payType;
}
