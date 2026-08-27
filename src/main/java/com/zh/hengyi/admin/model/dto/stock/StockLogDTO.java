package com.zh.hengyi.admin.model.dto.stock;

import com.zh.hengyi.admin.model.entity.stock.Stock;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockLogDTO {
    // 变更前库存
    private Stock beforeStock;
    // 变更后库存
    private Stock afterStock;

    private Long orderId;
    private String orderSn;
    private Long seckillGoodsId;

    private Integer changeType;
    private Integer changeNum;

    private String remark;
}
