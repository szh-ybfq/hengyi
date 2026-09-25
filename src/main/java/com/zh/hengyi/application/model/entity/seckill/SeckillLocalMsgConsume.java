package com.zh.hengyi.application.model.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.zh.hengyi.application.model.entity.BaseEntity;
import lombok.Data;

/**
 * 
 * @TableName seckill_local_msg_consume
 */
@TableName(value ="seckill_local_msg_consume")
@Data
public class SeckillLocalMsgConsume extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一id
     */
    private String msgId;

    /**
     * 队列名称，防止不同队列msgId重复
     */
    private String queueName;

    /**
     * 状态：0处理中 1消费成功 2消费失败
     */

}