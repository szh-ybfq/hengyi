package com.zh.hengyi.application.model.dto.stock;

import com.zh.hengyi.application.model.entity.seckill.SeckillGoods;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockLogSeckillDTO {
    private SeckillGoods beforeStock;
    private SeckillGoods afterStock;
    private Long skuId;
    private Long orderId;
    private String orderSn;
    private Long seckillGoodsId;

    private Integer changeType;
    private Integer changeNum;

    private String remark;
}
