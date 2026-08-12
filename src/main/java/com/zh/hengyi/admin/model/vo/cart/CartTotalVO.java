package com.zh.hengyi.admin.model.vo.cart;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartTotalVO {
    // 选中商品总数量
    private Integer totalCount;
    // 选中商品总金额
    private BigDecimal totalAmount;
    // 购物车全部商品列表
    private List<CartVO> cartList;
}