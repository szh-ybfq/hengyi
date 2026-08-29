package com.zh.hengyi.admin.model.vo.seckill;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityVO {
    private Long id;
    private String activityName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    // 编辑回显：本场秒杀商品
    private List<SeckillGoodsVO> goodsList;
}
