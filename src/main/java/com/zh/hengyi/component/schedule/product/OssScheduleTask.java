package com.zh.hengyi.component.schedule.product;

import cn.hutool.core.collection.CollUtil;
import com.zh.hengyi.application.service.product.ProductImageService;
import com.zh.hengyi.config.oss.aliyun.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssScheduleTask {

    private final ProductImageService productImageService;
    private final AliOssUtil aliOssUtil;

    /**
     * 每天凌晨2点执行清理OSS垃圾图片
     * 逻辑：数据库存在的url集合；OSS遍历全部文件；OSS有DB没有即为垃圾
     * <p>
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 20 * * ?")
    public void clearOssGarbageImage() {
        log.info("【OSS垃圾图片定时清理任务开始】");
        try {
            //1 查询数据库所有有效图片url
            List<String> urlList = productImageService.getImageUrlList();
            Set<String> urlSet = new HashSet<>();
            if(CollUtil.isNotEmpty(urlList)){
                urlSet.addAll(urlList);
            }

            //2 获取OSS该业务目录下全部文件url集合
            // 这里需要你自己实现一个工具方法 aliOssUtil.listFileUrl() 返回该目录下全部文件url列表
            List<String> ossFileUrlList = aliOssUtil.listFileUrl();
            if(CollUtil.isEmpty(ossFileUrlList)){
                log.info("OSS没有文件，任务结束");
                return;
            }
            int delCount = 0;
            //遍历OSS文件，找出DB不存在的url，就是垃圾
            for (String ossUrl : ossFileUrlList) {
                if(!urlSet.contains(ossUrl)){
                    try {
                        aliOssUtil.deleteByUrl(ossUrl);
                        delCount++;
                    } catch (Exception e) {
                        log.error("删除OSS垃圾文件失败 url:{}",ossUrl,e);
                    }
                }
            }
            log.info("【OSS垃圾图片定时清理任务结束】清理垃圾数量:{}",delCount);
        }catch (Exception e){
            log.error("OSS垃圾定时任务整体异常",e);
        }
    }
}
