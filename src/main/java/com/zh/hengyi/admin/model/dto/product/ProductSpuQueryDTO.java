package com.zh.hengyi.admin.model.dto.product;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "SPU分页查询DTO")
public class ProductSpuQueryDTO extends BaseQueryDTO {
    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "分类id")
    @Min(value = 1,message = "分类ID不能小于1")
    private Long categoryId;

    @Schema(description = "0下架 1上架")
    private Integer status;
}