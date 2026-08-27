package com.zh.hengyi.admin.model.vo.cart;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartVO {
    // 购物车主键id todo:待修复：目前返回前端时为null
    private Long id;
    // 用户id todo:可以考虑删掉，多余字段，没必要返回前端
    private Long userId;
    // sku id
    private Long skuId;
    // spu商品名称
    private String spuName;
    // sku规格文字 如 黑色/256G
    private String skuSpec;
    // 商品图片
    private String picUrl;
    // 单价
    private BigDecimal price;
    // 购买数量
    private Integer count;
    // 当前小计
    private BigDecimal subTotal;
    // 是否选中 0未选 1选中
    private Integer selected;
    // 库存剩余
    private Integer stock;
}