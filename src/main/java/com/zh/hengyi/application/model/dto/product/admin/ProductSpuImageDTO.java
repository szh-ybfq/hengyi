package com.zh.hengyi.application.model.dto.product.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SPU编辑DTO")
public class ProductSpuImageDTO {
    @Schema(description = "spu主键id")
    private Long id;

    @Schema(description = "spu主图列表")
    private List<String> mainImgList;

    @Schema(description = "spu详情图片集合")
    private List<String> detailImgList;

    @Schema(description = "spu参数图片集合")
    private List<String> paramImgList;
}