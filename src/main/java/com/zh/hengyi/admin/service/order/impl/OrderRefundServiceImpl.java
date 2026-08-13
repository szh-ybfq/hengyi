//package com.zh.hengyi.admin.service.order.impl;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.lang.UUID;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.zh.hengyi.admin.mapper.order.OrderRefundMapper;
//import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
//import com.zh.hengyi.admin.model.entity.order.Order;
//import com.zh.hengyi.admin.model.entity.order.OrderRefund;
//import com.zh.hengyi.admin.model.vo.order.OrderRefundVO;
//import com.zh.hengyi.admin.service.order.OrderRefundService;
//import com.zh.hengyi.common.constant.OrderConstant;
//import com.zh.hengyi.common.exception.BusinessException;
//import com.zh.hengyi.common.result.ResultCode;
//import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements OrderRefundService {
//    private final OrderRefundMapper refundMapper;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void applyRefund(OrderRefundApplyDTO dto) {
//        Long userId = SecurityUtils.getLoginUser().getUser().getId();
//        // 1. 查询订单校验
//        Order order = baseMapper.selectById(dto.getOrderId());
//        if(order == null){
//            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
//        }
//        // 权限校验：仅本人订单可退款
//        if(!order.getUserId().equals(userId)){
//            throw new BusinessException(ResultCode.ORDER_OPERATE_FORBID);
//        }
//        // 状态校验：仅已支付/已发货允许申请退款
//        Integer status = order.getOrderStatus();
//        if(!(Objects.equals(status, OrderConstant.ORDER_HAVING_PAY) || Objects.equals(status, OrderConstant.ORDER_HAVING_SEND))){
//            throw new BusinessException(ResultCode.REFUND_FORBID_STATUS);
//        }
//        // 校验是否已有退款申请
//        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(OrderRefund::getOrderId, dto.getOrderId());
//        OrderRefund exist = refundMapper.selectOne(wrapper);
//        if(exist != null){
//            throw new BusinessException(ResultCode.REFUND_EXIST);
//        }
//
//        // 2. 创建退款记录
//        OrderRefund refund = new OrderRefund();
//        refund.setOrderId(dto.getOrderId());
//        refund.setRefundSn(UUID.fastUUID().toString(true));
//        refund.setRefundAmount(order.getPayAmount());
//        refund.setRefundStatus(OrderConstant.ORDER_REFUND_DOING);
//        refund.setRefundReason(dto.getRefundReason());
//        refund.setCreateBy(userId);
//        refund.setCreateTime(LocalDateTime.now());
//        refundMapper.insert(refund);
//
//        // 3. 更新订单状态为退款中
//        Order updateOrder = new Order();
//        updateOrder.setId(dto.getOrderId());
//        updateOrder.setOrderStatus(OrderConstant.ORDER_HAVING_REFUND);
//        baseMapper.updateById(updateOrder);
//        log.info("用户{}对订单{}发起退款申请", userId, order.getOrderSn());
//        // TODO 迭代：退款成功回调、库存回滚
//    }
//}