package com.zh.hengyi.admin.model.entity.stock;

import com.baomidou.mybatisplus.annotation.*;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品库存表
 * @TableName stock
 */
@TableName(value ="stock")
@Data
public class Stock extends BaseEntity  {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联SKU ID
     */
    private Long skuId;
    /**
     * 秒杀库存（秒杀模块用，普通订单不用）
     */
    private Integer seckillStock;
    /**
     * 可售库存（用户能下单的库存）
     */
    private Integer availableStock;
    /**
     * 锁定库存（下单未支付，暂时锁住）
     */
    private Integer lockedStock;
    /**
     * 已售库存（支付成功后累加）
     */
    private Integer soldStock;
    /**
     * 乐观锁版本号
     */
    @Version
    private Long version;
}