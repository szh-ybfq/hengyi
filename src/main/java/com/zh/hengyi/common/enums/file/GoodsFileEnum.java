package com.zh.hengyi.common.enums.file;


import lombok.Getter;

@Getter
public enum GoodsFileEnum {
    COMMON("common/", "商品通用文件"),

    // 商品
    GOODS_MAIN("goods/main/","商品主图"),
    GOODS_DETAILS("goods/details/","商品详情图"),
    GOODS_PARAMS("goods/params/","商品参数图");

    private final String dir;
    private final String desc;

    GoodsFileEnum(String dir, String desc) {
        this.dir = dir;
        this.desc = desc;
    }
}

