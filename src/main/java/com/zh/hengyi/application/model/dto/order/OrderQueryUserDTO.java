package com.zh.hengyi.application.model.dto.order;

import com.zh.hengyi.application.model.dto.BaseQueryDTO;
import lombok.Data;

@Data
public class OrderQueryUserDTO extends BaseQueryDTO {

    // 订单编号模糊查询
    private String orderSn;

    // 订单状态筛选
    private Integer orderStatus;

}