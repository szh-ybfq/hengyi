package com.zh.hengyi.admin.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.entity.order.OrderRefund;
import com.zh.hengyi.admin.model.vo.order.OrderRefundVO;

/**
* @author HENGGE
* @description 针对表【order_refund(订单退款记录表)】的数据库操作Service
* @createDate 2026-08-12 20:25:57
*/
public interface OrderRefundService extends IService<OrderRefund> {
    // 申请退款
    void applyRefund(OrderRefundApplyDTO dto);
}
