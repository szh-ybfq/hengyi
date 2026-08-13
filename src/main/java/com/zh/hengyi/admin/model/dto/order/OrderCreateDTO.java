package com.zh.hengyi.admin.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCreateDTO {
    // 用户下单备注
    private String remark;
}