package com.zh.hengyi.admin.model.vo.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRefundVO {
    private Long id;
    private String refundSn;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private String refundReason;
    private LocalDateTime refundTime;
}