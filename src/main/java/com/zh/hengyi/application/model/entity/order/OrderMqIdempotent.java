package com.zh.hengyi.application.model.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zh.hengyi.application.model.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;

/**
 * 
 * @TableName order_mq_idempotent
 */
@TableName(value ="order_mq_idempotent")
@Data
@Builder
public class OrderMqIdempotent extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一标识
     */
    private String msgId;

    /**
     * 业务id（订单id）
     */
    private Long businessId;

}