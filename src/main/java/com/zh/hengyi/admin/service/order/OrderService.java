package com.zh.hengyi.admin.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.order.OrderCreateDTO;
import com.zh.hengyi.admin.model.dto.order.OrderCreateResDTO;
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
    void createOrder(OrderCreateDTO dto);

    IPage<OrderPageVO> getMyOrderPage(OrderQueryUserDTO dto);

    IPage<OrderPageVO> getAdminOrderPage(OrderQueryAdminDTO dto);

    OrderDetailVO getOrderDetail(Long orderId);

    void cancelOrder(Long orderId);

    void closeOrderByTimeout(Long orderId);

    Order validOrderExist(Long orderId);

    void validOrderSelf(Order order,Long userId);

    void validOrderStatusIsNoPay(Integer orderStatus);

    void validOrderIsCancel(Order order);

    void setOrderCancelStatus(Long orderId);

}
