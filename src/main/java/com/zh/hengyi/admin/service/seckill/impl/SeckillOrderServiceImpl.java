package com.zh.hengyi.admin.service.seckill.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderItemMapper;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillActivityMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillGoodsMapper;
import com.zh.hengyi.admin.mapper.stock.StockLogMapper;
import com.zh.hengyi.admin.model.dto.seckill.*;
import com.zh.hengyi.admin.model.dto.stock.StockLogDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogSeckillDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.entity.seckill.SeckillActivity;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import com.zh.hengyi.admin.model.entity.stock.StockLog;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.admin.service.seckill.SeckillGoodsService;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import com.zh.hengyi.admin.service.stock.StockLogService;
import com.zh.hengyi.admin.service.stock.StockService;
import com.zh.hengyi.common.constant.OrderConstant;
import com.zh.hengyi.common.constant.SeckillConstant;
import com.zh.hengyi.common.constant.StockConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.security.UserUtils;
import com.zh.hengyi.component.rabbitmq.order.OrderDelayProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.constant.SeckillConstant.*;
import static com.zh.hengyi.config.rabbitmq.SeckillRabbitConfig.SECKILL_DIRECT_EXCHANGE;
import static com.zh.hengyi.config.rabbitmq.SeckillRabbitConfig.SECKILL_ORDER_ROUTING_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillGoodsMapper,SeckillGoods> implements SeckillOrderService {

    private final SeckillGoodsMapper seckillGoodsMapper;
    private final SeckillGoodsService seckillGoodsService;
    private final SeckillActivityService seckillActivityService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockLogMapper stockLogMapper;
    private final ProductSpuService spuService;
    private final ProductSkuService skuService;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;
    private final OrderDelayProducer orderDelayProducer;

    // 1.1 Redis层拦截，发送MQ消息返回
    @Override
    public void submitSeckillOrder(SeckillOrderCreateDTO dto) {
        //校验用户是否登录
        Long userId = UserUtils.validUserLogin().getId();
        Long seckillGoodsId = dto.getSeckillGoodsId();
        Integer buyCount = dto.getCount();

        // 校验秒杀活动是否开启
        seckillActivityService.validSeckillActivityStartBySeckillGoods(seckillGoodsId);

        //1、Redis读取每人限购数量
        int limitPerson = Integer.parseInt(redissonClient.getBucket(SECKILL_USER_LIMIT_PREFIX + seckillGoodsId).get().toString());

        //2、校验用户是否超过每人限购（根据已购 +本次购买）
        String userBuyKey = SECKILL_USER_BUY_PREFIX + seckillGoodsId + ":" + userId;
        Object userBuyNum = redissonClient.getBucket(userBuyKey).get();
        int userAlreadyBuy = userBuyNum == null ? 0 : Integer.parseInt(userBuyNum.toString());

        if(userAlreadyBuy + buyCount > limitPerson){
            throw new BusinessException(ResultCode.SECKILL_OUT_USER_LIMIT);
        }

        //3、Lua脚本：Redis原子扣减秒杀缓存库存，同时标记用户已购买；防止超卖
        List<Object> keys = List.of(SECKILL_STOCK_PREFIX + seckillGoodsId, userBuyKey);
        String luaScript = """
                local stockKey = KEYS[1]
                local userBuyKey = KEYS[2]
                local buyNum = tonumber(ARGV[1])
                local limit = tonumber(ARGV[2])
                local alreadyBuy = tonumber(redis.call('get',userBuyKey) or 0)
                if(alreadyBuy + buyNum > limit) then
                    return -1
                end
                local stock = tonumber(redis.call('get',stockKey) or 0)
                if stock < buyNum then
                    return -2
                end
                redis.call('decrby',stockKey,buyNum)
                redis.call('incrby',userBuyKey,buyNum)
                -- 设置用户购买标记过期时间，和订单超时时间一致30分钟
                redis.call('expire',userBuyKey,1800)
                return 0
                """;
        Long luaResult = redissonClient.getScript().eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.LONG,
                keys,
                buyCount,
                limitPerson);
        if(luaResult == -1){
            throw new BusinessException(ResultCode.SECKILL_OUT_USER_LIMIT);
        }
        if(luaResult == -2){
            throw new BusinessException(ResultCode.SECKILL_STOCK_SHORTAGE);
        }

        //4、全部Redis校验通过，组装MQ消息，发送消息队列，直接响应前端，不阻塞等待DB
        SeckillOrderMsgDTO msgDTO = new SeckillOrderMsgDTO();
        msgDTO.setMsgId(UUID.fastUUID().toString(true));
        msgDTO.setUserId(userId);
        msgDTO.setSeckillGoodsId(seckillGoodsId);
        msgDTO.setCount(buyCount);
        msgDTO.setRemark(dto.getRemark());

        //发送持久化消息
        CorrelationData correlationData = new CorrelationData(msgDTO.getMsgId());
        rabbitTemplate.convertAndSend(SECKILL_DIRECT_EXCHANGE,SECKILL_ORDER_ROUTING_KEY,msgDTO,correlationData);
        log.info("秒杀请求入MQ成功，msgId:{},userId:{},seckillGoodsId:{}",msgDTO.getMsgId(),userId,seckillGoodsId);
    }


    // 1.2 MQ消费者调用，执行下秒杀单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeSeckillOrder(SeckillOrderMsgDTO msgDTO) {
        Long userId = msgDTO.getUserId();
        Long seckillGoodsId = msgDTO.getSeckillGoodsId();
        Integer buyCount = msgDTO.getCount();

        // 获取分布式锁，防止数据库层并发超卖
        RLock lock = redissonClient.getLock(SECKILL_LOCK_PREFIX + seckillGoodsId);
        boolean getLock = false;
        try {
            getLock = lock.tryLock(0,30, TimeUnit.SECONDS);
            if(!getLock){
                log.warn("获取秒杀分布式锁失败，msgId:{}",msgDTO.getMsgId());
                return;
            }

            // 1、校验：秒杀商品是否存在
            SeckillGoods seckillGoods = seckillGoodsService.validSeckillGoodsExist(seckillGoodsId);

            // 2、校验：可用秒杀库存是否充足
            int realRemainStock = seckillGoods.getSeckillStock() - seckillGoods.getSeckillSold();
            if(realRemainStock < buyCount){
                throw new BusinessException(ResultCode.SECKILL_STOCK_SHORTAGE);
            }

            // 3、秒杀下单预占库存
            int row = seckillGoodsMapper.lockSeckillGoodsStock(seckillGoodsId, buyCount, seckillGoods.getVersion());
            if(row == 0){
                throw new BusinessException(ResultCode.STOCK_OPTIMISTIC_LOCK_FAIL);
            }

            // 4、构建秒杀订单主表
            BigDecimal totalAmount = seckillGoods.getSeckillPrice().multiply(new BigDecimal(buyCount));
            String orderSn = UUID.fastUUID().toString(true);
            Order order = Order.builder()
                    .orderSn(orderSn)
                    .orderType(SeckillConstant.ORDER_STATUS_SECKILL)
                    .userId(userId)
                    .totalAmount(totalAmount)
                    .payAmount(totalAmount)
                    .orderStatus(OrderConstant.ORDER_NO_PAY)
                    .remark(msgDTO.getRemark())
                    .build();
            orderMapper.insert(order);
            Long orderId = order.getId();

            // 5、构建订单子项
            ProductSku sku = skuService.validSkuExist(seckillGoods.getSkuId());
            ProductSpu spu = spuService.validSpuExist(sku.getSpuId());
            OrderItem orderItem = OrderItem.builder()
                    .orderId(orderId)
                    .spuId(spu.getId())
                    .spuName(spu.getSpuName())
                    .skuId(sku.getId())
                    .skuSpec(sku.getSkuSpec())
                    .price(seckillGoods.getSeckillPrice())
                    .count(buyCount)
                    .totalPrice(totalAmount)
                    .build();
            orderItemMapper.insert(orderItem);

            // 6、保存秒杀库存流水
            saveStockLog(StockLogSeckillDTO.builder()
                    .beforeStock(seckillGoods)
                    .afterStock(baseMapper.selectById(seckillGoodsId))
                    .orderId(orderId)
                    .orderSn(orderSn)
                    .seckillGoodsId(seckillGoodsId)
                    .changeType(StockConstant.CHANGE_TYPE_SECKILL_LOCK)
                    .changeNum(buyCount)
                    .remark("秒杀下单预占库存，订单号：" + orderSn)
                    .build()
            );

            // 7、发送30分钟延迟关单消息
            orderDelayProducer.sendOrderDelayMsg(orderId);
            log.info("MQ消费秒杀下单成功,msgId:{},orderId:{},orderSn:{}",msgDTO.getMsgId(),orderId,orderSn);

        }catch (Exception e){
            log.error("秒杀消费异常 msgId:{}",msgDTO.getMsgId(),e);
            throw new RuntimeException(e);
        }finally {
            if(getLock && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }






    @Override
    public Order validSeckillOrderExist(Long seckilLId){
        Order order = orderMapper.selectById(seckilLId);
        if(order == null){
            throw new BusinessException(ResultCode.SECKILL_ORDER_NOT_EXIST);
        }
        return order;
    }

    // 组装并保存库存流水（订单id、编号可为null）
    private void saveStockLog(StockLogSeckillDTO logDto){
        StockLog stockLog = new StockLog();

        stockLog.setSkuId(logDto.getBeforeStock().getSkuId());
        stockLog.setOrderId(logDto.getOrderId());
        stockLog.setOrderSn(logDto.getOrderSn());
        stockLog.setSeckillGoodsId(logDto.getSeckillGoodsId());

        stockLog.setChangeType(logDto.getChangeType());
        stockLog.setChangeNum(logDto.getChangeNum());

        stockLog.setBeforeAvailable(logDto.getBeforeStock().getSeckillStock());
        stockLog.setAfterAvailable(logDto.getAfterStock().getSeckillStock());
        stockLog.setBeforeLocked(logDto.getBeforeStock().getSeckillLock());
        stockLog.setAfterLocked(logDto.getAfterStock().getSeckillLock());

        stockLog.setRemark(logDto.getRemark());

        stockLogMapper.insert(stockLog);
    }
}



