package com.zh.hengyi.admin.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.mapper.order.OrderRefundMapper;
import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderRefund;
import com.zh.hengyi.admin.model.vo.order.OrderRefundVO;
import com.zh.hengyi.admin.service.order.OrderRefundService;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.common.constant.OrderConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.security.UserUtils;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements OrderRefundService {

    private final OrderRefundMapper refundMapper;
    private final OrderMapper orderMapper;

    // 1、申请退款
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(OrderRefundApplyDTO dto) {
        // 1、校验登录
        Long userId = UserUtils.validUserLogin().getId();

        // 2、校验订单存在
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 3、校验仅本人订单可退款
        validOrderRefundSelf(order, userId);

        // 4、状态校验：仅已支付、已发货允许申请退款
        validOrderRefundStatus(order);

        // 5、校验是否已有退款申请
        validOrderRefundExist(dto.getOrderId());

        // 6. 新增退款记录
        OrderRefund refund = OrderRefund.builder()
                                        .orderId(dto.getOrderId())
                                        .refundSn(UUID.fastUUID().toString(true))
                                        .refundAmount(order.getPayAmount())
                                        .refundStatus(OrderConstant.ORDER_DOING_REFUND)
                                        .refundReason(dto.getRefundReason())
                                        .build();
        refundMapper.insert(refund);

        // 3. 更新订单状态（退款中）
        Order updateOrder = Order.builder()
                .id(dto.getOrderId())
                .orderStatus(OrderConstant.ORDER_DOING_REFUND)
                .build();
        orderMapper.updateById(updateOrder);
        log.info("用户{}对订单{}发起退款申请", userId, order.getOrderSn());

        // TODO 迭代：退款成功回调、库存回滚
    }

    // 校验退款订单存在
    @Override
    public void validOrderRefundExist(Long orderId){
        OrderRefund orderRefund = baseMapper.selectOne(new LambdaQueryWrapper<OrderRefund>().eq(OrderRefund::getOrderId, orderId));
        if(orderRefund != null){
            throw new BusinessException(ResultCode.ORDER_REFUND_EXIST);
        }
    }

    // 校验退款订单是否非本人
    @Override
    public void validOrderRefundSelf(Order order,Long userId){
        if(ObjUtil.notEqual(order.getUserId(),userId)){
            throw new BusinessException(ResultCode.ORDER_NOT_SLEF_REFUND_OPERATE_FORBID);
        }
    }

    // 校验退款订单状态
    @Override
    public void validOrderRefundStatus(Order order){
        if(ObjUtil.notEqual(order.getStatus(), OrderConstant.ORDER_HAVING_PAY) && ObjUtil.notEqual(order.getStatus(), OrderConstant.ORDER_HAVING_SEND)){
            throw new BusinessException(ResultCode.ORDER_REFUND_OPERATE_FORBID);
        }
    }
}