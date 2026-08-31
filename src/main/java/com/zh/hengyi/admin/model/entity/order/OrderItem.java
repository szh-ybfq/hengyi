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
import java.util.Date;

/**
 * 订单子项明细表
 * @TableName order_item
 */
@TableName(value ="order_item")
@Data
@Builder
public class OrderItem extends BaseEntity  {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单id
     */
    private Long orderId;

    private Long spuId;

    private Long skuId;

    /**
     * 商品快照名称
     */
    private String spuName;

    /**
     * 规格快照
     */
    private String skuSpec;

    /**
     * 下单时单价快照
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 该商品小计
     */
    private BigDecimal totalPrice;

}