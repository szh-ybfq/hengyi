package com.zh.hengyi.application.mapper.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.application.model.entity.order.OrderItem;
import com.zh.hengyi.application.model.entity.seckill.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    int getGoodsBuyNumByUser(SeckillGoods seckillGoods);

    @Select("""
        SELECT SUM(oi.count)
                FROM `order` o
                INNER JOIN order_item oi ON o.id = oi.order_id
                WHERE o.user_id = #{userId}
                AND o.order_status NOT IN (4,5)
                AND oi.sku_id = #{skuId}
    """)
    Integer getUserBuySumBySkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);
}




