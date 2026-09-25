package com.zh.hengyi.application.controller.admin.goods;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.application.controller.admin.file.FileController;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuAddDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuEditDTO;
import com.zh.hengyi.application.model.dto.product.admin.ProductSpuQueryDTO;
import com.zh.hengyi.application.model.entity.file.FileBatchUploadVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuFormVO;
import com.zh.hengyi.application.model.vo.product.admin.ProductSpuPageVO;
import com.zh.hengyi.application.service.file.FileService;
import com.zh.hengyi.application.service.product.ProductImageService;
import com.zh.hengyi.application.service.product.ProductSpuService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/product/spu")
@Tag(name = "后台商品管理模块")
@RequiredArgsConstructor
public class ProductSpuController {

    private final ProductSpuService spuService;
    private final ProductImageService productImageService;


    @GetMapping("/page")
    @Operation(summary = "SPU商品分页")   // @Validated 针对Get RequestParam校验
    public Result<IPage<ProductSpuPageVO>> getPage(@Validated ProductSpuQueryDTO dto) {
        return Result.success(spuService.getPageByAdmin(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个商品详情")
    public Result<ProductSpuFormVO> getInfo(@PathVariable Long id) {
        return Result.success(spuService.getSpuInfo(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增SPU商品")
    public Result<Void> add(@Valid @RequestBody ProductSpuAddDTO dto) {
        spuService.add(dto);
        return Result.success();
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑SPU商品")
    public Result<Void> edit(@Valid @RequestBody ProductSpuEditDTO dto) {
        spuService.edit(dto);
        return Result.success();
    }

    /*    @PutMapping("/edit")
    @Operation(summary = "商品上下架")
    public Result<Void> edit(@Valid @RequestBody ProductSpuEditDTO dto) {
        spuService.edit(dto);
        return Result.success();
    }*/

    @DeleteMapping("/{id}")
    @Operation(summary = "删除SPU商品")
    public Result<Void> remove(@PathVariable Long id) {
        spuService.removeById(id);
        return Result.success();
    }


    @PostMapping("/upload/image")
    @Operation(summary = "上传单个图片")
    public Result<String> uploadImage(@RequestParam(value = "spuId",required = false) Long spuId,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("fileType") String fileType) throws IOException {
        return Result.success(productImageService.uploadImage(spuId,file,fileType));
    }

    @PostMapping("/upload/images")
    @Operation(summary = "批量上传图片")
    public Result<FileBatchUploadVO> batchUploadImage(@RequestParam(value = "spuId",required = false) Long spuId,
                                                      @RequestParam("file") List<MultipartFile> fileList,
                                                      @RequestParam("fileType") String fileType){
        return Result.success(productImageService.uploadImages(spuId,fileList,fileType));
    }


    @DeleteMapping("/image/delete")
    @Operation(summary = "删除单张图片")
    public Result<Void> deleteImage(@RequestParam("fileUrl") String imgUrl) {
        productImageService.deleteSingleImageByUrl(imgUrl);
        return Result.success();
    }

    @DeleteMapping("/image/deleteBatch")
    @Operation(summary = "删除多张图片")
    public Result<Void> deleteImages(@RequestParam("ids") List<String> imgUrls) {
        productImageService.deleteBatchImagesByUrl(imgUrls);
        return Result.success();
    }
}