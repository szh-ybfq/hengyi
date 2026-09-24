package com.zh.hengyi.application.service.order;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.order.OrderItemMapper;
import com.zh.hengyi.application.model.entity.order.OrderItem;
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




