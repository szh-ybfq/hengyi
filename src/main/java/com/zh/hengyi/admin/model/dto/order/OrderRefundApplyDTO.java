package com.zh.hengyi.admin.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRefundApplyDTO {
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;
}
