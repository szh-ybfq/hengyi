package com.zh.hengyi.admin.model.vo.stock;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockLogVO {
    private Long id;
    private Long skuId;
    private Long orderId;
    private Long seckillGoodsId;
    private Integer changeType;
    private Integer changeNum;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeLocked;
    private Integer afterLocked;
    private String remark;
    private LocalDateTime createTime;
}
