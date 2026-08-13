package com.zh.hengyi.admin.mapper.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.weaver.ast.Or;

import java.util.List;

/**
* @author HENGGE
* @description 针对表【order_item(订单子项明细表)】的数据库操作Mapper
* @createDate 2026-08-12 20:25:57
* @Entity generator.domain.OrderItem
*/
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    default List<OrderItem> selectByOrderId(Long orderId){
        return selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,orderId));
    };
}




