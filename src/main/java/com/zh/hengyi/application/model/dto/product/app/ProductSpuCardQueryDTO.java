package com.zh.hengyi.application.model.dto.product.app;

import com.zh.hengyi.application.model.dto.BaseQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "C端商品分页查询DTO")
public class ProductSpuCardQueryDTO extends BaseQueryDTO {
    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "分类id，默认查全部")
    private Long categoryId;
}
