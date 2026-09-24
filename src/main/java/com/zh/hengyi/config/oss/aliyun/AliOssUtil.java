package com.zh.hengyi.config.oss.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

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
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
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
}
