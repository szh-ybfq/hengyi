package com.zh.hengyi.admin.model.dto.seckill;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillOrderMsgDTO implements Serializable {
    // 消息唯一id，用于幂等消费
    private String msgId;
    private Long userId;
    private Long seckillGoodsId;
    private Integer count;
    private String remark;
}

