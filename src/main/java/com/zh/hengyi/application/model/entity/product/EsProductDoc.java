//package com.zh.hengyi.admin.model.entity.product;
//
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
//@Data
//@Document(indexName = "product_spu_index") //索引名，相当于mysql的表
//public class EsProductDoc {
//    @Id
//    private Long id;
//
//    //商品名称：text类型，支持分词检索，ik分词
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
//    private String spuName;
//
//    @Field(type = FieldType.Integer)
//    private Long categoryId;
//
//    @Field(type = FieldType.Double)
//    private Double price;
//
//    @Field(type = FieldType.Integer)
//    private Integer saleCount;
//
//    @Field(type = FieldType.Integer)
//    private Integer status; //上下架状态
//
//    @Field(type = FieldType.Keyword)
//    private String img;
//}
