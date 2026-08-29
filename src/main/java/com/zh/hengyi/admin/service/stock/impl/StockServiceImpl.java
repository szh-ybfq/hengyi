package com.zh.hengyi.admin.service.stock.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.mapper.stock.StockMapper;
import com.zh.hengyi.admin.model.dto.product.ProductSkuAddDTO;
import com.zh.hengyi.admin.model.dto.stock.StockDeductDTO;
import com.zh.hengyi.admin.model.dto.stock.StockEditDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogDTO;
import com.zh.hengyi.admin.model.dto.stock.StockRollbackDTO;
import com.zh.hengyi.admin.model.entity.BaseEntity;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import com.zh.hengyi.admin.model.entity.stock.StockLog;
import com.zh.hengyi.admin.model.vo.stock.StockVO;
import com.zh.hengyi.admin.service.stock.StockLogService;
import com.zh.hengyi.admin.service.stock.StockService;
import com.zh.hengyi.common.constant.StockConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.convert.ConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final StockMapper stockMapper;
    private final StockLogService stockLogService;
    private final ProductSpuMapper spuMapper;

    // 1 下单   预占库存          痛点解决：乐观锁防止并发超卖，每条操作写入流水日志对账
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockStock(StockDeductDTO dto) {
        for (StockDeductDTO.SkuNumDTO skuNum : dto.getSkuNumList()) {
            // 1.1 校验库存记录是否存在
            Long skuId = skuNum.getSkuId();
            Integer count = skuNum.getCount();
            Stock stock = validStockExist(skuId);

            // 1.2 校验可用库存是否充足
            validStockAvailable(stock,count);

            // 2.乐观锁预扣库存（执行时发现，version发生变化，说明另外线程已经修改，扣减失败，返回异常，重新下单）
            int row = stockMapper.lockStock(skuId, count, stock.getVersion());
            if (row == 0) {
                throw new BusinessException(ResultCode.STOCK_OPTIMISTIC_LOCK_FAIL, "商品sku:" + skuId + "库存并发抢占失败，请重试");
            }
            // 3.保存库存流水
            saveStockLog(StockLogDTO.builder()
                    .beforeStock(stock)
                    .afterStock(baseMapper.selectById(stock.getId()))
                    .orderId(dto.getOrderId())
                    .orderSn(dto.getOrderSn())
                    .changeType(StockConstant.CHANGE_TYPE_LOCK)
                    .changeNum(count)
                    .remark("下单预占库存，订单号：" + dto.getOrderSn())
                    .build());
        }
        log.info("订单{}，预占库存全部完毕", dto.getOrderSn());
    }

    // 2 支付成功   扣减锁定库存，转入已售
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockAfterPay(StockDeductDTO dto) {
        for (StockDeductDTO.SkuNumDTO skuNum : dto.getSkuNumList()) {
            // 1. 乐观锁扣减库存（下单时已经校验库存记录存在，不用再重复校验）
            Long skuId = skuNum.getSkuId();
            Integer count = skuNum.getCount();
            Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getId, skuId));
            int row = stockMapper.deductStockAfterPay(skuId, count, stock.getVersion());
            if (row == 0) {
                throw new BusinessException(ResultCode.STOCK_LOCK_SHORT, "支付扣减库存失败，锁定库存不足");
            }
            // 2. 保存库存流水
            saveStockLog(StockLogDTO.builder()
                    .beforeStock(stock)
                    .afterStock(baseMapper.selectById(stock.getId()))
                    .orderId(dto.getOrderId())
                    .orderSn(dto.getOrderSn())
                    .seckillGoodsId(null)
                    .changeType(StockConstant.CHANGE_TYPE_DEDUCT)
                    .changeNum(count)
                    .remark("支付成功扣减库存，订单号：" + dto.getOrderSn())
                    .build());
        }
        log.info("订单{}支付成功，库存扣减完成", dto.getOrderSn());
    }

    // 3 订单取消回滚     锁定库存至可售
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackCancelStock(StockRollbackDTO dto) {
        List<StockRollbackDTO.SkuNumDTO> skuNumList = dto.getSkuNumList();
        for (StockRollbackDTO.SkuNumDTO skuNum : skuNumList) {
            // 1. 订单取消回滚库存
            Long skuId = skuNum.getSkuId();
            Integer count = skuNum.getCount();
            Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
            int row = stockMapper.rollbackStockByCancel(skuId, count, stock.getVersion());
            if (row == 0) {
                throw new BusinessException(ResultCode.STOCK_ROLLBACK_FAIL, "取消订单库存回滚失败");
            }
            // 2. 保存库存流水
            saveStockLog(StockLogDTO.builder()
                    .beforeStock(stock)
                    .afterStock(baseMapper.selectById(stock.getId()))
                    .orderId(dto.getOrderId())
                    .orderSn(null)
                    .seckillGoodsId(null)
                    .changeType(StockConstant.CHANGE_TYPE_CANCEL_ROLLBACK)
                    .changeNum(count)
                    .remark(dto.getRemark())
                    .build());
        }
        log.info("订单{}取消，库存回滚完成", dto.getOrderId());
    }

    // 4 退款回滚   已售库存至可售
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackRefundStock(StockRollbackDTO dto) {
        List<StockRollbackDTO.SkuNumDTO> skuNumList = dto.getSkuNumList();
        for (StockRollbackDTO.SkuNumDTO skuNum : skuNumList) {
            // 1. 订单取消回滚库存
            Long skuId = skuNum.getSkuId();
            Integer count = skuNum.getCount();
            Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
            int row = stockMapper.rollbackStockByRefund(skuId, count, stock.getVersion());
            if (row == 0) {
                throw new BusinessException(ResultCode.STOCK_ROLLBACK_FAIL, "退款库存回滚失败");
            }
            // 2. 保存库存流水
            saveStockLog(StockLogDTO.builder()
                    .beforeStock(stock)
                    .afterStock(baseMapper.selectById(stock.getId()))
                    .orderId(dto.getOrderId())
                    .orderSn(null)
                    .seckillGoodsId(null)
                    .changeType(StockConstant.CHANGE_TYPE_REFUND_ROLLBACK)
                    .changeNum(count)
                    .remark(dto.getRemark())
                    .build());
        }
        log.info("订单{}退款，库存回滚完成", dto.getOrderId());
    }

/*
    // 5 秒杀订单取消回滚
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackSeckillStock(StockRollbackDTO dto) {
        List<StockRollbackDTO.SkuNumDTO> skuNumList = dto.getSkuNumList();
        for (StockRollbackDTO.SkuNumDTO skuNum : skuNumList) {
            // 1. 订单取消回滚库存
            Long skuId = skuNum.getSkuId();
            Integer count = skuNum.getCount();
            Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
            int row = stockMapper.rollbackStockByRefund(skuId, count, stock.getVersion());
            if (row == 0) {
                throw new BusinessException(ResultCode.STOCK_ROLLBACK_FAIL, "退款库存回滚失败");
            }
            // 2. 保存库存流水
            saveStockLog(StockLogDTO.builder()
                    .beforeStock(stock)
                    .afterStock(baseMapper.selectById(stock.getId()))
                    .orderId(dto.getOrderId())
                    .orderSn(null)
                    .seckillGoodsId(null)
                    .changeType(StockConstant.CHANGE_TYPE_REFUND_ROLLBACK)
                    .changeNum(count)
                    .remark(dto.getRemark())
                    .build());
        }
        log.info("订单{}退款，库存回滚完成", dto.getOrderId());
    }
*/

    // 5 批量创建库存记录，新增商品SKU时调用
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateStock(Map<Long,Integer> skuStockMap) {
        List<Stock> stockList = new ArrayList<>();
        skuStockMap.entrySet().forEach(entry -> {
            Stock stock = new Stock();
            stock.setSkuId(entry.getKey());
            stock.setAvailableStock(entry.getValue());
            stock.setLockedStock(StockConstant.LOCKED_STOCK);
            stock.setSoldStock(StockConstant.SOLD_STOCK);
            stock.setSeckillStock(StockConstant.SECKILL_STOCK);
            stock.setVersion(StockConstant.VERSION);
            stockList.add(stock);
        });
        this.saveBatch(stockList);
    }

    // 6 批量删除库存记录（修改商品删除旧SKU / 删除整个SPU）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchLogicDeleteStock(List<Long> skuIdList) {
        if (CollUtil.isEmpty(skuIdList)) {
            throw new BusinessException(ResultCode.STOCK_SKU_LIST_EMPTY);
        }
        // 写法1 删除变修改 只改deleted字段即可
        baseMapper.update(new LambdaUpdateWrapper<Stock>().in(Stock::getSkuId, skuIdList).set(BaseEntity::getDeleted,1));
    }

    // 7 后台手动调整库存
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editStock(StockEditDTO dto) {
        Long skuId = dto.getSkuId();
        Integer adjustNum = dto.getAdjustNum();
        Stock stock = validStockExist(skuId);
        // 正数加库存，负数减库存，校验调整后库存不能为负
        Integer newAvailable = stock.getAvailableStock() + adjustNum;
        if (newAvailable < 0) {
            throw new BusinessException(ResultCode.STOCK_ADJUST_NEGATIVE, "调整后可用库存不能为负数");
        }
        Stock update = new Stock();
        update.setId(stock.getId());
        update.setAvailableStock(newAvailable);
        update.setVersion(stock.getVersion() + 1);
        updateById(update);

        saveStockLog(StockLogDTO.builder()
                .beforeStock(stock)
                .afterStock(baseMapper.selectById(stock.getId()))
                .orderId(null)
                .orderSn(null)
                .seckillGoodsId(null)
                .changeType(StockConstant.CHANGE_TYPE_ADMIN_EDIT)
                .changeNum(Math.abs(adjustNum))
                .remark("后台手动调整库存：" + dto.getRemark())
                .build());
    }

    // 8 根据skuId获取库存
    @Override
    public StockVO getStockBySkuId(Long skuId) {
        // 校验skuid是否为空
        if (skuId==null) {
            return null;
        }
        // 校验sku是否存在
        Stock stock = validStockExist(skuId);
        return BeanUtil.copyProperties(stock, StockVO.class);
    }

    // 9 根据skuIds批量获取库存
    @Override
    public List<StockVO> getStockListBySkuIds(List<Long> skuIds) {
        // 校验skuIds是否为空
        if (CollUtil.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        List<Stock> stocks = baseMapper.selectList(new LambdaQueryWrapper<Stock>().in(Stock::getSkuId, skuIds));
        // 拿到数据库存在的skuId集合
        Set<Long> existSkuIdSet = stocks.stream().map(Stock::getSkuId).collect(Collectors.toSet());
        // 找出缺失库存记录的skuId集合
        List<Long> notExistSkuIds = skuIds.stream()
                .filter(id -> !existSkuIdSet.contains(id))
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(notExistSkuIds)){
            // 将缺失库存的skuIds返回给前端
            throw new BusinessException(ResultCode.STOCK_NOT_EXIST,"sku:{"+notExistSkuIds+"}，未配置库存");
        }
        return ConvertUtils.convertList(stocks, StockVO.class);
    }

    // 10 秒杀商品扣减剩余库存
    @Override
    public void deductAvailableStock(Stock stock,Integer seckillStock){
        // 乐观锁扣减可用库存（执行时发现，version发生变化，说明另外线程已经修改，扣减失败）
        Long skuId = stock.getSkuId();
        int result = stockMapper.deductAvailableStock(skuId, seckillStock, stock.getVersion());
        if (result == 0) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_AVAILABLE_STOCK_DEDUCT_FAIL);
        }
        // 保存库存流水
        saveStockLog(StockLogDTO.builder()
                .beforeStock(stock)
                .afterStock(baseMapper.selectById(stock.getId()))
                .seckillGoodsId(skuId)
                .changeType(StockConstant.CHANGE_TYPE_SECKILL_DEDUCT)
                .changeNum(seckillStock)
                .remark("秒杀商品sku id：{}，扣减可用库存成功" + skuId)
                .build());
        log.info("秒杀商品sku id：{}，扣减可用库存成功，并写入库存流水",skuId);
    };

    // 11 秒杀商品归还可用库存
    @Override
    public void revertAvailableStock(Stock stock,Integer num){
        Long skuId = stock.getSkuId();
        // 乐观锁归还可用库存（执行时发现，version发生变化，说明另外线程已经修改，归还失败）
        int result = stockMapper.revertAvailableStock(skuId, num, stock.getVersion());
        if (result == 0) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_REVERT_STOCK__FAIL);
        }
        // 保存库存流水
        saveStockLog(StockLogDTO.builder()
                .beforeStock(stock)
                .afterStock(baseMapper.selectById(stock.getId()))
                .seckillGoodsId(skuId)
                .changeType(StockConstant.CHANGE_TYPE_SECKILL_REVERT)
                .changeNum(num)
                .remark("秒杀商品sku id：{}，归还库存成功" + skuId)
                .build());
        log.info("秒杀商品sku id：{}，归还库存成功，并写入库存流水",skuId);
    };

    // 校验sku库存记录存在
    @Override
    public Stock validStockExist(Long skuId) {
        Stock stock = baseMapper.selectOne(new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
        if (stock == null) {
            throw new BusinessException(ResultCode.STOCK_NOT_EXIST);
        }
        return stock;
    }

    // 校验sku可用库存是否充足
    @Override
    public void validStockAvailable(Stock stock,Integer count) {
        if (stock.getAvailableStock() < count) {
            // todo:前端返回可以优化，返回商品名，编号，规格信息，别写什么sku啥的
            throw new BusinessException(ResultCode.STOCK_SHORTAGE, "商品sku:" + stock.getSkuId() + "可用库存不足");
        }
    }

    // 组装并保存库存流水（订单id、编号可为null）
    @Override
    public void saveStockLog(StockLogDTO logDto){
        StockLog stockLog = new StockLog();

        stockLog.setSkuId(logDto.getBeforeStock().getSkuId());
        stockLog.setOrderId(logDto.getOrderId());
        stockLog.setOrderSn(logDto.getOrderSn());
        stockLog.setSeckillGoodsId(logDto.getSeckillGoodsId());

        stockLog.setChangeType(logDto.getChangeType());
        stockLog.setChangeNum(logDto.getChangeNum());

        stockLog.setBeforeAvailable(logDto.getBeforeStock().getAvailableStock());
        stockLog.setAfterAvailable(logDto.getAfterStock().getAvailableStock());
        stockLog.setBeforeLocked(logDto.getBeforeStock().getLockedStock());
        stockLog.setAfterLocked(logDto.getAfterStock().getLockedStock());

        stockLog.setRemark(logDto.getRemark());

        stockLogService.save(stockLog);
    }
}
