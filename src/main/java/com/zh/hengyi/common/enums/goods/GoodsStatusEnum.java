package com.zh.hengyi.common.enums.goods;


import lombok.Getter;

@Getter
public enum GoodsStatusEnum {
    GOODS_UP(1,"商品上架"),
    GOODS_DOWN(0,"商品下架");

    private final Integer status;
    private final String desc;

    GoodsStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}

