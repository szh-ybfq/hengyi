package com.zh.hengyi.admin.mapper.stock;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【stock(商品库存表)】的数据库操作Mapper
* @createDate 2026-08-14 09:37:47
* @Entity generator.domain.Stock
*/
@Mapper
public interface StockMapper extends BaseMapper<Stock>{
    /**
     * 1.用户下单：可售-数量，锁定+数量
     */
    default int lockStock(Long skuId, Integer num, Long version) {
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("available_stock = available_stock - " + num)
                .setSql("locked_stock = locked_stock + " + num)
                .setSql("version = version + 1")
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getVersion, version)
                .ge(Stock::getAvailableStock, num);
        return update(updateWrapper);
    }

    /**
     * 2.支付成功扣减：锁定-数量，已售+数量
     */
    default int deductStockAfterPay(Long skuId, Integer num, Long version) {
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("locked_stock = locked_stock - " + num)
                .setSql("sold_stock = sold_stock + " + num)
                .setSql("version = version + 1")
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getVersion, version)
                .ge(Stock::getLockedStock, num);
        return update(updateWrapper);
    }

    /**
     * 3.取消订单回滚：锁定-数量，可售+数量
     */
    default int rollbackStockByCancel(Long skuId, Integer num, Long version) {
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("locked_stock = locked_stock - " + num)
                .setSql("available_stock = available_stock + " + num)
                .setSql("version = version + 1")
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getVersion, version)
                .ge(Stock::getLockedStock, num);
        return update(updateWrapper);
    }

    /**
     * 4.退款回滚：已售-数量，可售+数量
     */
    default int rollbackStockByRefund(Long skuId, Integer num, Long version) {
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("sold_stock = sold_stock - " + num)
                .setSql("available_stock = available_stock + " + num)
                .setSql("version = version + 1")
                .eq(Stock::getSkuId, skuId)
                .eq(Stock::getVersion, version)
                .ge(Stock::getSoldStock, num);
        return update(updateWrapper);
    }
}






