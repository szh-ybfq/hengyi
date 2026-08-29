package com.zh.hengyi.admin.model.dto.seckill;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityFormDTO {
    private Long id;

    private String activityName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    // 表单同时传秒杀商品集合
    private List<SeckillGoodsAddDTO> goodsList;
}

