package com.zh.hengyi.admin.model.dto.seckill;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillOrderCreateDTO {

    @NotNull(message = "秒杀商品id不能为空")
    private Long seckillGoodsId;

    @Min(value = 1, message = "购买数量至少为1")
    private Integer count;

    private String remark;
}
