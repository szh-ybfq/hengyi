package com.zh.hengyi.admin.service.pay;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.pay.PayCallbackDTO;
import com.zh.hengyi.admin.model.dto.pay.PayCreateDTO;
import com.zh.hengyi.admin.model.entity.pay.PayRecord;
import com.zh.hengyi.admin.model.vo.pay.PayRecordVO;

public interface PayRecordService extends IService<PayRecord> {
    /** 创建支付单，生成支付流水号 */
    PayRecordVO createPayRecord(PayCreateDTO dto);
    /** 模拟支付回调，更新支付状态，支付成功扣库存 */
    void payCallback(PayCallbackDTO dto);
    /** 根据订单id查询支付记录 */
    PayRecordVO getPayByOrderId(Long orderId);
}
