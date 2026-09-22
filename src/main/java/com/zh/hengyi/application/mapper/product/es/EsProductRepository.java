//package com.zh.hengyi.admin.mapper.product.es;
//
//import com.zh.hengyi.admin.model.entity.product.EsProductDoc;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.elasticsearch.annotations.Query;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//
//public interface EsProductRepository extends ElasticsearchRepository<EsProductDoc, Long> {
//    Page<EsProductDoc> findBySpuNameContainingAndCategoryIdAndStatus(String spuName, Long categoryId, Integer status, Pageable pageable);
//    Page<EsProductDoc> findBySpuNameContainingAndStatus(String spuName, Integer status, Pageable pageable);
//    Page<EsProductDoc> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
//}
