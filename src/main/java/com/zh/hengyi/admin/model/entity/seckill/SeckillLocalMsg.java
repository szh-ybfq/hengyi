package com.zh.hengyi.admin.model.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.baomidou.mybatisplus.annotation.Version;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import lombok.Data;

/**
 * 秒杀本地消息表(本地消息表实现最终一致性)
 * @TableName seckill_local_msg
 */
@TableName(value ="seckill_local_msg")
@Data
public class SeckillLocalMsg extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 全局唯一消息id，幂等key
     */
    private String msgId;

    /**
     * 消息json内容
     */
    private String msgContent;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由key
     */
    private String routingKey;

    @Version
    private Long version;

    /**
     * 状态： 0待发送，1已发送，2发送失败
     */
    private Integer retryCount;

}