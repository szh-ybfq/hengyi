package com.zh.hengyi.admin.model.entity.pay;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 支付流水记录表
 * @TableName payment_record
 */
@TableName(value ="pay_record")
@Data
public class PayRecord extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联订单ID
     */
    private Long orderId;
    /**
     * 订单编号（冗余）
     */
    private String orderSn;
    /**
     * 支付流水号（模拟第三方支付单号，唯一）
     */
    private String paySn;
    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;
    /**
     * 支付方式 1微信 2支付宝 3模拟测试支付
     */
    private Integer payType;
    /**
     * 0待支付 1支付成功 2支付失败 3已退款
     */
    private Integer payStatus;
    /**
     * 支付回调原始报文
     */
    private String callbackContent;
    /**
     * 支付完成时间
     */
    private LocalDateTime paySuccessTime;
    /**
     * 支付失败时间
     */
    private LocalDateTime payFailTime;

}