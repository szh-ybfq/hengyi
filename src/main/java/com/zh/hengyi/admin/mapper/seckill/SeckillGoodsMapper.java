package com.zh.hengyi.admin.mapper.seckill;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HENGGE
 * @description 针对表【seckill_goods(秒杀商品表)】的数据库操作Mapper
 * @createDate 2026-08-28 07:28:08
 * @Entity generator.domain.SeckillGoods
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    // 秒杀下单预占库存：增加锁定seckill_lock，乐观锁version
    default int lockSeckillGoodsStock(Long seckillGoodsId, Integer count, Long version) {
        return update(new LambdaUpdateWrapper<SeckillGoods>()
                    .setSql("seckill_stock = seckill_stock - " + count)
                    .setSql("seckill_lock = seckill_lock + " + count)
                    .setSql("version = version + 1")
                    .eq(SeckillGoods::getId, seckillGoodsId)
                    .eq(SeckillGoods::getVersion, version)
                    .ge(SeckillGoods::getSeckillStock, count)
        );
    }

    // 支付成功：扣锁定库存，加到已售出 seckill_sold += count , seckill_lock -= count
    default int deductLockToSold(Long seckillGoodsId, Integer count, Long version) {
        return update(new LambdaUpdateWrapper<SeckillGoods>()
                    .setSql("seckill_lock = seckill_lock - " + count)
                    .setSql("seckill_sold = seckill_sold + " + count)
                    .setSql("version = version + 1")
                    .eq(SeckillGoods::getId, seckillGoodsId)
                    .eq(SeckillGoods::getVersion, version)
                    .ge(SeckillGoods::getSeckillLock, count)
        );
    }
    // 订单取消/超时关闭：释放锁定库存 seckill_lock -= count
    default int rollbackLockStock(Long seckillGoodsId, Integer count, Long version) {
        return update(new LambdaUpdateWrapper<SeckillGoods>()
                    .setSql("seckill_stock = seckill_stock + " + count)
                    .setSql("seckill_lock = seckill_lock - " + count)
                    .setSql("version = version + 1")
                    .eq(SeckillGoods::getId, seckillGoodsId)
                    .eq(SeckillGoods::getVersion, version)
                    .ge(SeckillGoods::getSeckillLock, count));
    }
    // 退款回滚：已售-数量，可售+数量
    default int rollbackStockByRefund(Long seckillGoodsId, Integer count, Long version) {
        return update(new LambdaUpdateWrapper<SeckillGoods>()
                    .setSql("seckill_stock = sold_stock + " + count)
                    .setSql("seckill_sold = seckill_sold - " + count)
                    .setSql("version = version + 1")
                    .eq(SeckillGoods::getId, seckillGoodsId)
                    .eq(SeckillGoods::getVersion, version)
                    .ge(SeckillGoods::getSeckillSold, count)
        );
    }
}
