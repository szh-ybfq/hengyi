package com.zh.hengyi.application.model.entity.file;

import lombok.Data;

import java.util.List;

@Data
public class FileBatchUploadVO {
    /**成功url列表*/
    private List<String> successUrlList;
    /**失败数量*/
    private Integer failCount;
    /**失败提示信息*/
    private String failMsg;
}