package com.zh.hengyi.admin.model.dto.pay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayCallbackDTO {
    @NotBlank(message = "支付流水号不能为空")
    private String paySn;
    @NotNull(message = "支付状态 1成功 2失败")
    private Integer payStatus;
    // 模拟第三方回调原始报文
    private String callbackContent;
}
