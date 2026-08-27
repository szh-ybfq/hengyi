package com.zh.hengyi.admin.service.stock;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.product.ProductSkuAddDTO;
import com.zh.hengyi.admin.model.dto.product.ProductSpuAddDTO;
import com.zh.hengyi.admin.model.dto.stock.StockDeductDTO;
import com.zh.hengyi.admin.model.dto.stock.StockEditDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogDTO;
import com.zh.hengyi.admin.model.dto.stock.StockRollbackDTO;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import com.zh.hengyi.admin.model.vo.stock.StockVO;
import java.util.List;
import java.util.Map;

public interface StockService extends IService<Stock> {
    /** 下单预占库存（乐观锁） */
    void lockStock(StockDeductDTO dto);
    /** 支付成功真实扣减库存 */
    void deductStockAfterPay(StockDeductDTO dto);
    /** 取消订单回滚库存 */
    void rollbackCancelStock(StockRollbackDTO dto);
    /** 退款回滚库存 */
    void rollbackRefundStock(StockRollbackDTO dto);
    /**
     * 批量创建sku库存记录
     */
    void batchCreateStock(Map<Long,Integer> skuStockMap);
    /**
     * 批量逻辑删除库存记录
     */
    void batchLogicDeleteStock(List<Long> skuIdList);
    /** 后台手动调整库存 */
    void editStock(StockEditDTO dto);
    /** 根据skuId查询库存 */
    StockVO getStockBySkuId(Long skuId);
    /** 批量查询sku库存 */
    List<StockVO> getStockListBySkuIds(List<Long> skuIds);
    /** 校验sku库存记录存在，不存在自动初始化库存 */
    Stock validStockExist(Long skuId);
    /** 校验sku可用库存是否充足 */
    void validStockAvailable(Stock stock,Integer count);
    /** 保存库存流水 */
    void saveStockLog(StockLogDTO logDto);
}
