//package com.zh.hengyi.admin.service.product.es;
//
//import cn.hutool.core.util.StrUtil;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.zh.hengyi.admin.mapper.product.es.EsProductRepository;
//import com.zh.hengyi.admin.model.entity.product.EsProductDoc;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//
//import static com.zh.hengyi.common.constant.ProductConstant.PRODUCT_STATUS_UP;
//
//@Service
//public class EsProductSearchService {
//    private final EsProductRepository esProductRepository;
//
//    public EsProductSearchService(EsProductRepository esProductRepository) {
//        this.esProductRepository = esProductRepository;
//    }
//
//    public List<EsProductDoc> searchProduct(String keyword, Long categoryId, Integer pageNum, Integer pageSize){
//        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
//
//        Page<EsProductDoc> pageResult;
//        if(StrUtil.isNotBlank(keyword) && categoryId != null){
//            // 根据关键字、分类
//            pageResult = esProductRepository.findBySpuNameContainingAndCategoryIdAndStatus(keyword,categoryId,PRODUCT_STATUS_UP,pageRequest);
//        }else if(StrUtil.isNotBlank(keyword)){
//            // 根据关键字
//            pageResult = esProductRepository.findBySpuNameContainingAndStatus(keyword,PRODUCT_STATUS_UP,pageRequest);
//        }else if(categoryId != null){
//            // 根据分类
//            pageResult = esProductRepository.findByCategoryIdAndStatus(categoryId,PRODUCT_STATUS_UP,pageRequest);
//        }else {
//            // 无查询参数，返回空集合
//            return Collections.emptyList();
//        }
//        return pageResult.getContent();
//    }
//
//
//    //新增/更新ES文档
//    public void saveDoc(EsProductDoc doc){
//        esProductRepository.save(doc);
//    }
//
//    //删除ES文档
//    public void deleteDoc(Long spuId){
//        esProductRepository.deleteById(spuId);
//    }
//}
