package com.zh.hengyi.admin.model.dto.order;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import lombok.Data;

@Data
public class OrderQueryUserDTO extends BaseQueryDTO {
    // 订单状态筛选
    private Integer orderStatus;
    // 订单编号模糊查询
    private String orderSn;
}