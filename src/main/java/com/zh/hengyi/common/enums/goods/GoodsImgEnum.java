package com.zh.hengyi.common.enums.goods;


import lombok.Getter;

@Getter
public enum GoodsImgEnum {
    MAIN_IMG_NUM(1,"商品主图图片数量"),
    MAIN_IMG(1,"商品主图类型"),
    DETAIL_IMG(2,"商品详情图类型"),
    PARAM_IMG(3,"商品参数图类型");

    private final Integer type;
    private final String desc;


    GoodsImgEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}

