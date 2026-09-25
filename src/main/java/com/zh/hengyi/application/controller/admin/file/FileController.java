package com.zh.hengyi.application.controller.admin.file;

import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import com.zh.hengyi.application.service.file.FileService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/*
* 仅上传到oss，不上传到数据库
* */
@RestController
@RequestMapping("/admin/api/v1/file")
@Tag(name = "后台OSS文件管理模块")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /*@GetMapping Mapping("/get")
    @Operation(summary = "获取文件")
    public Result<byte[]> uploadFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("fileType") String fileType) throws IOException {
        aliOssUtil.getOssFileBytes(file,fileType);
        return Result.success();
    }*/

    @PostMapping("/upload/image")
    @Operation(summary = "上传单个图片")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                      @RequestParam("fileType") String fileType) throws IOException {
        return Result.success(fileService.uploadImage(file,fileType));
    }

    @PostMapping("/upload/images")
    @Operation(summary = "批量上传图片")
    public Result<FileBatchUploadVO> batchUploadImage(@RequestParam("file") List<MultipartFile> fileList,
                                                      @RequestParam("fileType") String fileType){
        return Result.success(fileService.uploadImages(fileList,fileType));
    }

    // todo: 上传文档、视频待完善
    /*@PostMapping("/upload/doc")
    @Operation(summary = "上传文档")
    public Result<String> uploadDoc(@RequestParam("file") MultipartFile file,
                                  @RequestParam("fileType") String fileType) throws IOException {
        return Result.success(fileService.uploadImage(file,fileType));
    }

    @PostMapping("/upload/video")
    @Operation(summary = "上传视频")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file,
                                  @RequestParam("fileType") String fileType) throws IOException {
        return Result.success(fileService.uploadImage(file,fileType));
    }*/

    // 删除根据具体业务需求去写，因为不仅删除oss，还要删除表记录
}