package com.zh.hengyi.application.model.vo.product.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "SPU图片回显VO")
public class ProductSpuImageVO {
    private List<String> mainImgList;
    private List<String> detailImgList;
    private List<String> paramImgList;
}