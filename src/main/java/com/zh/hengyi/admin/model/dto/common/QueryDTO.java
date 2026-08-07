package com.zh.hengyi.admin.model.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询父类")
public class QueryDTO {

    @Schema(description = "页码")
    private Long pageNum;

    @Schema(description = "每页条数")
    private Long pageSize;
}
