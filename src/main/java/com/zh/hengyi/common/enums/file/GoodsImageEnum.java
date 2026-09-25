package com.zh.hengyi.common.enums.file;


import lombok.Getter;

@Getter
public enum     GoodsImageEnum {
    COMMON("common/",0, "商品通用文件"),

    // 商品
    GOODS_MAIN("goods/main/",1,"商品主图"),
    GOODS_DETAILS("goods/details/",2,"商品详情图"),
    GOODS_PARAMS("goods/params/",3,"商品参数图");

    private final String dir;
    private final Integer type;
    private final String desc;

    GoodsImageEnum(String dir, Integer type, String desc) {
        this.dir = dir;
        this.type = type;
        this.desc = desc;
    }
}

