package com.zh.hengyi.admin.model.vo.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderPageVO {
    private Long id;
    private String orderSn;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private LocalDateTime createTime;
    // 当前订单商品简略列表
    private List<OrderItemVO> itemList;
}