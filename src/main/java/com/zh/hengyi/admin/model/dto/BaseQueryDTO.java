package com.zh.hengyi.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "分页查询父类")
public class BaseQueryDTO {

    @Schema(description = "页码")
    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum;

    @Schema(description = "每页条数")
    @Min(value = 1, message = "每页条数不能小于1")
    private Long pageSize;
}
