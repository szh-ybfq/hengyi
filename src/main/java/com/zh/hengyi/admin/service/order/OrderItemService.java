package com.zh.hengyi.admin.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.entity.order.OrderItem;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【order_item(订单子项明细表)】的数据库操作Service
* @createDate 2026-08-12 20:25:57
*/
public interface OrderItemService extends IService<OrderItem> {
    List<OrderItem> getByOrderId(Long orderId);
}
