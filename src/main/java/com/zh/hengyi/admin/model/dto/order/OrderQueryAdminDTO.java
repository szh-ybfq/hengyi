package com.zh.hengyi.admin.model.dto.order;

import com.zh.hengyi.admin.model.dto.BaseQueryDTO;
import lombok.Data;

@Data
public class OrderQueryAdminDTO extends BaseQueryDTO {
    // 订单编号模糊查询
    private String orderSn;
    // 订单状态筛选
    private Integer orderStatus;
    // 用户id
    private Long userId;

}