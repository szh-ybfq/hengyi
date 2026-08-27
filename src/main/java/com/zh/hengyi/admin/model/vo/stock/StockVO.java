package com.zh.hengyi.admin.model.vo.stock;
import lombok.Data;

@Data
public class StockVO {
    private Long id;
    private Long skuId;
    private Integer seckillStock;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer soldStock;
}
