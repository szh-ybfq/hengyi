package com.zh.hengyi.admin.model.entity.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存变更流水表
 * @TableName stock_log
 */
@TableName(value ="stock_log")
@Data
public class StockLog extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 商品SKU ID
     */
    private Long skuId;
    /**
     * 关联订单ID
     */
    private Long orderId;
    /**
     * 订单编号
     */
    private String orderSn;
    /**
     * 秒杀商品ID，普通订单为null
     */
    private Long seckillGoodsId;
    /**
     * 变更类型 1下单锁定 2支付扣减 3取消回补 4退款回补 5后台手动调整
     */
    private Integer changeType;
    /**
     * 变更数量（正数）
     */
    private Integer changeNum;
    /**
     * 操作前可售库存
     */
    private Integer beforeAvailable;
    /**
     * 操作后可售库存
     */
    private Integer afterAvailable;
    /**
     * 操作前锁定库存
     */
    private Integer beforeLocked;
    /**
     * 操作后锁定库存
     */
    private Integer afterLocked;
    /**
     * 操作备注
     */
    private String remark;

}