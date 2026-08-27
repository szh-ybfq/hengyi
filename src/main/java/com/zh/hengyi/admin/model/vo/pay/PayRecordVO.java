package com.zh.hengyi.admin.model.vo.pay;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayRecordVO {
    private Long id;
    private Long orderId;
    private String orderSn;
    private String paySn;
    private BigDecimal payAmount;
    private Integer payType;
    private Integer payStatus;
    private LocalDateTime paySuccessTime;
    private LocalDateTime payFailTime;
    private String callbackContent;
    private LocalDateTime createTime;
}
