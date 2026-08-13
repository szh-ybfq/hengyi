package com.zh.hengyi.admin.mapper.order;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zh.hengyi.admin.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【order(订单主表)】的数据库操作Mapper
* @createDate 2026-08-12 20:25:57
* @Entity generator.domain.Order
*/
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    default IPage<Order> selectUserOrderPage(Page<Order> page, Long userId , OrderQueryUserDTO dto){
        //只查当前用户订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                //批量拼接条件即可
                .eq(Order::getUserId, userId)
                .like(StrUtil.isNotBlank(dto.getOrderSn()), Order::getOrderSn, dto.getOrderSn())
                .eq(dto.getOrderStatus()!=null, Order::getOrderStatus, dto.getOrderStatus())
                .orderByDesc(Order::getCreateTime);
        return selectPage(page, wrapper);
    };

    default IPage<Order> selectAdminOrderPage(Page<Order> page , OrderQueryAdminDTO dto){
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                //批量拼接条件即可
                .eq(dto.getUserId() !=null, Order::getUserId, dto.getUserId())
                .like(StrUtil.isNotBlank(dto.getOrderSn()), Order::getOrderSn, dto.getOrderSn())
                .eq(dto.getOrderStatus()!=null, Order::getOrderStatus, dto.getOrderStatus())
                .orderByDesc(Order::getCreateTime);
        return selectPage(page, wrapper);
    };
}




