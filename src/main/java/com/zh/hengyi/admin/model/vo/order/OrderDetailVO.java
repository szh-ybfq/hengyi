package com.zh.hengyi.admin.model.vo.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//
@Data
public class OrderDetailVO {
    private Long id;
    private String orderSn;
    private Long userId;
    private BigDecimal totalAmount;
    // 实付金额
    private BigDecimal payAmount;
    private Long couponId;
    private Integer orderStatus;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String remark;
    private LocalDateTime createTime;
    // 订单项集合
    private List<OrderItemVO> itemList;
    // 退款记录（存在退款则返回）
    private OrderRefundVO refund;
}