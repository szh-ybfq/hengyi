package com.zh.hengyi.application.model.dto.pay;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Jackson 反序列化 JSON 需要无参构造函数
public class PayCreateDTO {
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotNull(message = "支付方式不能为空 1微信 2支付宝 3测试支付")
    private Integer payType;
}
