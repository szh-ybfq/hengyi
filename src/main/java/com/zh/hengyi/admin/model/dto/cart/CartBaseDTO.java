package com.zh.hengyi.admin.model.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartBaseDTO {

    @NotNull(message = "skuId不能为空")
    private Long skuId;

    @Min(value = 1, message = "购买数量最少为1")
    @NotNull(message = "购买数量不能为空")
    private Integer count;
}