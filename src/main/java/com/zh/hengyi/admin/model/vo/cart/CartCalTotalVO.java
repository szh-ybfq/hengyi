package com.zh.hengyi.admin.model.vo.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartCalTotalVO {
    // 选中商品总件数
    private Integer totalCount;
    // 选中商品总金额
    private BigDecimal totalAmount;
}