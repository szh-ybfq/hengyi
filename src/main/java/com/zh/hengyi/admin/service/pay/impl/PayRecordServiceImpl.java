package com.zh.hengyi.admin.service.pay.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderItemMapper;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.mapper.pay.PayRecordMapper;
import com.zh.hengyi.admin.model.dto.pay.PayCallbackDTO;
import com.zh.hengyi.admin.model.dto.pay.PayCreateDTO;
import com.zh.hengyi.admin.model.dto.stock.StockDeductDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import com.zh.hengyi.admin.model.entity.pay.PayRecord;
import com.zh.hengyi.admin.model.vo.pay.PayRecordVO;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.admin.service.pay.PayRecordService;
import com.zh.hengyi.admin.service.stock.StockService;
import com.zh.hengyi.common.constant.OrderConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.constant.PayConstant.PAY_NO;
import static com.zh.hengyi.common.constant.PayConstant.PAY_TYPE_TEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayRecordServiceImpl extends ServiceImpl<PayRecordMapper, PayRecord> implements PayRecordService {

    private final PayRecordMapper payRecordMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;
    private final StockService stockService;


    // 创建支付单，hutool生成唯一paySn支付流水号
        // 为什么分开为两个事务？
        // ①无关联，下单只与库存有关，支付是支付模块，不能因为支付失败啥的，订单就消失了吧
        // ②拓展性强，后期下单后先不支付，支付时再创建支付单）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRecordVO createPayRecord(PayCreateDTO dto) {
        Long orderId = dto.getOrderId();
        // 1.校验订单存在、状态为待支付
        Order order = orderService.validOrderExist(orderId);
        if (!OrderConstant.ORDER_NO_PAY.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_PAY_FORBID);
        }

        // 2.校验是否已存在支付单（一单一条支付记录）
        PayRecord exist = payRecordMapper.selectByOrderId(orderId);
        if (exist != null) {
            return BeanUtil.copyProperties(exist, PayRecordVO.class);
        }

        // 3.构建支付记录
        PayRecord pay = new PayRecord();
        String paySn = UUID.fastUUID().toString(true); //生成支付流水号
        pay.setOrderId(orderId);
        pay.setOrderSn(order.getOrderSn());
        pay.setPaySn(UUID.fastUUID().toString(true));
        pay.setPayAmount(order.getPayAmount());
        pay.setPayType(PAY_TYPE_TEST);//todo:实际支付方式
        pay.setPayStatus(PAY_NO);
        save(pay);
        log.info("订单{}创建支付单成功，支付流水号：{}", order.getOrderSn(), paySn);
        return BeanUtil.copyProperties(pay, PayRecordVO.class);
    }

    /**
     * 模拟支付回调，支付成功触发库存扣减、更新订单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payCallback(PayCallbackDTO dto) {
        String paySn = dto.getPaySn();
        Integer payStatus = dto.getPayStatus();
        // 1.查询支付单
        PayRecord payRecord = payRecordMapper.selectByPaySn(paySn);
        if (payRecord == null) {
            throw new BusinessException(ResultCode.PAY_RECORD_NOT_EXIST);
        }
        // 2.重复回调拦截
        if (!payRecord.getPayStatus().equals(0)) {
            log.warn("支付流水{}已处理，重复回调直接忽略", paySn);
            throw new BusinessException(ResultCode.PAY_REPEAT_CALLBACK);
        }
        LocalDateTime now = LocalDateTime.now();

        PayRecord updatePay = new PayRecord();
        updatePay.setId(payRecord.getId());
        updatePay.setCallbackContent(dto.getCallbackContent());
        // 3.支付成功
        if (payStatus == 1) {
            updatePay.setPayStatus(1);
            updatePay.setPaySuccessTime(now);

            // 3.1 更新订单状态为已支付
            Long orderId = payRecord.getOrderId();
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
            }
            order.setOrderStatus(OrderConstant.ORDER_HAVING_PAY);
            order.setPayTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 3.2 查询订单项，真实扣减库存
            List<OrderItem> itemList = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            if (itemList == null || itemList.isEmpty()) {
                throw new BusinessException(ResultCode.PAY_ORDER_ITEM_EMPTY);
            }
            List<StockDeductDTO.SkuNumDTO> skuNumList = itemList.stream().map(item -> {
                StockDeductDTO.SkuNumDTO num = new StockDeductDTO.SkuNumDTO();
                num.setSkuId(item.getSkuId());
                num.setCount(item.getCount());
                return num;
            }).collect(Collectors.toList());
            StockDeductDTO stockDTO = new StockDeductDTO();
            stockDTO.setOrderId(orderId);
            stockDTO.setOrderSn(order.getOrderSn());
            stockDTO.setSkuNumList(skuNumList);
            try {
                stockService.deductStockAfterPay(stockDTO);
            } catch (BusinessException e) {
                log.error("支付回调扣减库存失败，订单号:{},异常:{}", order.getOrderSn(), e.getMessage());
                throw new BusinessException(ResultCode.PAY_STOCK_DEDUCT_FAIL);
            }
            log.info("支付回调成功，订单{}库存扣减完成", order.getOrderSn());
        } else {
            // 4.支付失败
            updatePay.setPayStatus(2);
            updatePay.setPayFailTime(now);
            log.info("支付流水{}支付失败", paySn);
            throw new BusinessException(ResultCode.PAY_TRADE_FAIL);
        }
        updateById(updatePay);
    }

    /**
     * 根据订单查询支付记录
     */
    @Override
    public PayRecordVO getPayByOrderId(Long orderId) {
        PayRecord pay = payRecordMapper.selectByOrderId(orderId);
        return BeanUtil.copyProperties(pay, PayRecordVO.class);
    }
}
