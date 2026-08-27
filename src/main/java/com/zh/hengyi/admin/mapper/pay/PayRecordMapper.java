package com.zh.hengyi.admin.mapper.pay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.pay.PayRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author HENGGE
* @description 针对表【payment_record(支付流水记录表)】的数据库操作Mapper
* @createDate 2026-08-14 09:37:38
* @Entity generator.domain.PaymentRecord
*/
@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {

    /** 根据订单id查询支付记录（一对一） */
    default PayRecord selectByOrderId(@Param("orderId") Long orderId){
        return selectOne(new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getOrderId,orderId));
    };

    /** 根据支付流水号查询 */
    default PayRecord selectByPaySn(@Param("paySn") String paySn){
        return selectOne( new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPaySn,paySn));
    };
}





