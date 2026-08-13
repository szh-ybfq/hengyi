package com.zh.hengyi.admin.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.order.OrderCreateDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.vo.order.OrderDetailVO;
import com.zh.hengyi.admin.model.vo.order.OrderPageVO;

/**
* @author HENGGE
* @description 针对表【order(订单主表)】的数据库操作Service
* @createDate 2026-08-12 20:25:57
*/
public interface OrderService extends IService<Order> {
    // 用户下单
    void createOrder(OrderCreateDTO dto);
    // 用户查询我的订单分页
    IPage<OrderPageVO> getMyOrderPage(OrderQueryUserDTO dto);
    // 后台查询所有订单分页
    IPage<OrderPageVO> getAdminOrderPage(OrderQueryAdminDTO dto);
    // 获取订单详情
    OrderDetailVO getOrderDetail(Long orderId);
    // 用户取消未支付订单
    void cancelOrder(Long orderId);
}
