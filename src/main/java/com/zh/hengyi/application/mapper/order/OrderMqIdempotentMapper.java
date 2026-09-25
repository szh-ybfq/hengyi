package com.zh.hengyi.application.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.application.model.entity.order.OrderMqIdempotent;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【order_mq_idempotent】的数据库操作Mapper
* @createDate 2026-08-13 20:52:21
* @Entity generator.domain.OrderMqIdempotent
*/
@Mapper
public interface OrderMqIdempotentMapper extends BaseMapper<OrderMqIdempotent> {

}




