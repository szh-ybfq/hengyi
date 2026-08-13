package com.zh.hengyi.admin.service.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderItemMapper;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import com.zh.hengyi.admin.service.order.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    private final OrderItemMapper orderItemMapper;
    @Override
    public List<OrderItem> getByOrderId(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }
}




