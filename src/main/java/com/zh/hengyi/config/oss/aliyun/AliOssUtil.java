package com.zh.hengyi.config.oss.aliyun;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.zh.hengyi.common.enums.file.GoodsImageEnum.*;

@Component
public class AliOssUtil {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;

    // 成员变量初始化早于构造器注入，故不推荐构造器注解注入
    public AliOssUtil(AliOssProperties aliOssProperties) {
        this.endpoint = aliOssProperties.getEndpoint();
        this.accessKeyId = aliOssProperties.getAccessKeyId();
        this.accessKeySecret = aliOssProperties.getAccessKeySecret();
        this.bucketName = aliOssProperties.getBucketName();
    }

    /**
     * 通用上传方法
     * @param file 上传文件
     * @param dir oss目录前缀，带后缀 例如：goods/main/
     * @return 完整访问url
     * <br/><br/>不返回异常，向上抛，由调用者自定义异常
     */
    public String upload(MultipartFile file, String dir) throws IOException {
        String originalFilename = file.getOriginalFilename();

        String suffix = ""; //后缀
        int len = originalFilename.lastIndexOf(".");
        if(len > 0){ // 防止上传图片无 “.”
            suffix = originalFilename.substring(len);
        }

        String objectName = dir + UUID.randomUUID() + suffix;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, file.getInputStream());
            return buildUrl(objectName);
        } finally {
            ossClient.shutdown();
        }
    }


    /**
     * 覆盖修改：上传指定objectName，覆盖已有文件，无真正修改方法
     * @param file 新文件
     * @param objectName oss里面完整对象名称 goods/main/xxx.jpg
     */
    public String uploadOverwrite(MultipartFile file, String objectName) throws IOException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, file.getInputStream());
            return buildUrl(objectName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 根据完整url解析objectName，删除oss文件
     * @param fileUrl
     */
    public void deleteByUrl(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            throw new BusinessException(ResultCode.IMG_HTTP_NOT_EXIST);
        }
        URL url;
        try {
            url = new URL(fileUrl);
            String objectName = url.getPath(); // bucket.oss-cn-guangzhou.aliyuncs.com/goods/main/xxx.jpg
            if (objectName.startsWith("/")) {  // /goods/main/xxx.jpg
                objectName = objectName.substring(1); // 去掉第一个/，goods/main/xxx.jpg
            }
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try {
                ossClient.deleteObject(bucketName, objectName);
            } finally {
                ossClient.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 访问OSS文件，返回文件字节数组（后端下载文件二进制，私有文件场景使用；公共读一般直接浏览器访问url）
     * @param objectName 对象名称
     * @return 文件字节数组
     */
    public byte[] getOssFileBytes(String objectName) throws IOException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        OSSObject ossObject = null;
        try {
            ossObject = ossClient.getObject(bucketName, objectName);
            //把输入流读到字节数组
            System.out.println(ossObject.getObjectContent().readAllBytes().toString());
            return ossObject.getObjectContent().readAllBytes();
        } finally {
            // 关闭所有资源
            if(ossObject != null){
                ossObject.close();
            }
            ossClient.shutdown();
        }
    }


    /**
     * 拼接完整访问url
     * @param objectName 对象名 goods/xxx.jpg
     * @return url https://bucket.oss‑cn‑guangzhou.aliyuncs.com/goods/main/xxx.jpg
     */
    private String buildUrl(String objectName) {
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    /**
     * 根据objectName生成访问url（公共读bucket直接返回）
     * <p>场景：上传图片存数据库</p>
     */
    public String getUrlByObjectName(String objectName) {
        return buildUrl(objectName);
    }

    /**
     * 列出商品图片目录下全部文件完整访问url
     * 注意：过滤2分钟以内新文件，避免事务还未提交定时任务误删
     * @return 完整http url集合
     */
    public List<String> listFileUrl(){
        List<String> resultUrlList = new ArrayList<>();
        List<String> prefixList = List.of(GOODS_MAIN.getDir(),GOODS_DETAILS.getDir(),GOODS_PARAMS.getDir());
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        Date now = new Date();
        for(String prefix : prefixList){
            String continuationToken = null;
            do {
                ListObjectsV2Request req = new ListObjectsV2Request()
                        .withBucketName(bucketName)
                        .withPrefix(prefix)
                        .withContinuationToken(continuationToken);
                ListObjectsV2Result resp = ossClient.listObjectsV2(req);
                for(OSSObjectSummary summary : resp.getObjectSummaries()){
                    //过滤2分钟以内新建文件，防止事务还没提交就被当做垃圾删掉
                    long diffMs = now.getTime() - summary.getLastModified().getTime();
                    if(diffMs < Duration.ofMinutes(2).toMillis()){
                        continue;
                    }
                    //拼接完整访问url
                    String fileUrl = buildUrl(summary.getKey());
                    resultUrlList.add(fileUrl);
                }
                continuationToken = resp.getNextContinuationToken();
            } while (continuationToken != null && !continuationToken.isEmpty());
        }
        return resultUrlList;
    }
}
