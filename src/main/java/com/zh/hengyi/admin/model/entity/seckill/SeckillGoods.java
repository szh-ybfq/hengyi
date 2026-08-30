package com.zh.hengyi.admin.model.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 秒杀商品表
 * @TableName seckill_goods
 */
@TableName(value ="seckill_goods")
@Data
public class SeckillGoods extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 秒杀活动id
     */
    private Long activityId;

    /**
     * 关联sku
     */
    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存
     */
    private Integer seckillStock;

    /**
     * 秒杀锁定
     */
    private Integer seckillLock;

    /**
     * 已售出
     */
    private Integer seckillSold;

    /**
     * 每人限购数量
     */
    private Integer limitPerson;

    /**
     * 乐观锁
     */
    private Integer version;

}