package com.zh.hengyi.admin.model.entity.order;

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
 * 订单主表
 * @TableName order
 */
@TableName(value ="`order`")//order是mysql内置关键字
@Data
@Builder
public class Order extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号，唯一
     */
    private String orderSn;

    /**
     * 下单用户id
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额，优惠券抵扣后
     */
    private BigDecimal payAmount;

    /**
     * 使用的用户优惠券id user_coupon.id
     */
    private Long couponId;

    /**
     * 0待支付 1已支付 2已发货 3已完成 4已取消 5退款
     */
    private Integer orderStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 取消关闭时间
     */
    private LocalDateTime cancelTime;

    /**
     * 用户备注
     */
    private String remark;
}