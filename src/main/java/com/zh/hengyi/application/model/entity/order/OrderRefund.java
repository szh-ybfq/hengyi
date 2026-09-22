package com.zh.hengyi.application.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.application.model.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款记录表
 * @TableName order_refund
 */
@TableName(value ="order_refund")
@Data
@Builder
public class OrderRefund extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单
     */
    private Long orderId;

    /**
     * 退款单号
     */
    private String refundSn;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 0申请中 1退款成功 2拒绝
     */
    private Integer refundStatus;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;

}