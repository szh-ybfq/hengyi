package com.zh.hengyi.admin.service.seckill.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderItemMapper;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.mapper.product.ProductSkuMapper;
import com.zh.hengyi.admin.mapper.product.ProductSpuMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillActivityMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillGoodsMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillLocalMsgMapper;
import com.zh.hengyi.admin.mapper.stock.StockLogMapper;
import com.zh.hengyi.admin.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.dto.seckill.*;
import com.zh.hengyi.admin.model.dto.stock.StockLogDTO;
import com.zh.hengyi.admin.model.dto.stock.StockLogSeckillDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.entity.seckill.SeckillActivity;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.entity.seckill.SeckillLocalMsg;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import com.zh.hengyi.admin.model.entity.stock.StockLog;
import com.zh.hengyi.admin.model.vo.order.OrderPageVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import com.zh.hengyi.admin.service.authority.UserService;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.admin.service.seckill.SeckillGoodsService;
import com.zh.hengyi.admin.service.seckill.SeckillLocalMsgService;
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
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.constant.SeckillConstant.*;
import static com.zh.hengyi.common.result.ResultCode.SECKILL_GOODS_DEGRADE;
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
    private final OrderService orderService;
    private final SeckillLocalMsgService seckillLocalMsgService;
    private final UserService userService;

    // 1.1 Redis+lua层拦截（削峰），发送MQ消息
    @Override
    @SentinelResource(
            value = "seckillSubmitResource", // Sentinel资源名称，配置规则用
            blockHandler = "seckillBlockHandler", // 限流、被拦截触发的方法
            fallback = "seckillFallbackHandler",    // 降级方法（默认捕获所有异常，💎要排除业务异常，否则业务异常如超出限购、空指针无法返回给前端）
            exceptionsToIgnore = {BusinessException.class}// 忽略业务异常，业务异常不会进入fallback降级
    )
    public void submitSeckillOrder(Long seckillGoodsId,SeckillOrderCreateDTO dto) {
        // 0.1 校验用户是否登录
        Long userId = UserUtils.validUserLogin().getId();
        Integer buyCount = dto.getCount();

        // 0.2 校验秒杀活动是否开启
        seckillActivityService.validSeckillActivityStartBySeckillGoods(seckillGoodsId);

        // 0.3 Redis连接异常手动降级(直接拒绝)
        try {
            redissonClient.getBucket(SECKILL_USER_LIMIT_PREFIX + seckillGoodsId).get();
        }catch (Exception ep){
            log.error("Redis连接异常，秒杀直接降级拒绝请求",ep);
            throw new BusinessException(ResultCode.SECKILL_REDIS_DEGRADE);
        }

        // 1、Redis读取每人限购数量
        int limitPerson = Integer.parseInt(redissonClient.getBucket(SECKILL_USER_LIMIT_PREFIX + seckillGoodsId).get().toString());

        // 2、校验用户是否超过每人限购（根据已购 +本次购买）
        // (一)“ 只有redis这一层缓存用户已购，缓存丢失超出限购还能卖 ”：解决：双校验，除了redis，还要在数据库加校验,见 consumeSeckillOrder 2 2.1 2.2
        String userBuyKey = SECKILL_USER_BUY_PREFIX + seckillGoodsId + ":" + userId;
        Object userBuyNum = redissonClient.getBucket(userBuyKey).get();
        int userAlreadyBuy = userBuyNum == null ? 0 : Integer.parseInt(userBuyNum.toString());

        if(userAlreadyBuy + buyCount > limitPerson){
            throw new BusinessException(ResultCode.SECKILL_OUT_USER_LIMIT);
        }

        // 3、Lua脚本：Redis原子扣减秒杀缓存库存，同时标记用户已购买；防止超卖
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
                -- 设置用户购买标记过期时间，和秒杀商品限购、秒杀库存保持一致，保证本场活动一致性
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

        // 4、组装MQ消息
        SeckillOrderMsgDTO msgDTO = new SeckillOrderMsgDTO();
        msgDTO.setMsgId(UUID.fastUUID().toString(true));
        msgDTO.setUserId(userId);
        msgDTO.setSeckillGoodsId(seckillGoodsId);
        msgDTO.setCount(buyCount);
        msgDTO.setRemark(dto.getRemark());

        // 5、保存本地消息（消息id，消息内容（即MQ消息））
        SeckillLocalMsg seckillLocalMsg = new SeckillLocalMsg();
        seckillLocalMsg.setMsgId(msgDTO.getMsgId());
        seckillLocalMsg.setMsgContent(JSON.toJSONString(msgDTO));
        seckillLocalMsg.setExchange(SECKILL_DIRECT_EXCHANGE);
        seckillLocalMsg.setRoutingKey(SECKILL_ORDER_ROUTING_KEY);
        seckillLocalMsg.setRetryCount(SECKILL_LOCAL_MSG_RETRY_COUNT_DEFAULT);
        seckillLocalMsgService.saveLocalMsg(seckillLocalMsg);

        log.info("秒杀请求，本地消息入库成功，msgId:{}",msgDTO.getMsgId());
    }

    // 1.2 MQ消费（填谷），执行下秒杀单
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

            // 1、校验是否存在 秒杀商品
            SeckillGoods seckillGoods = seckillGoodsService.validSeckillGoodsExist(seckillGoodsId);

            // 2、（双重校验）校验用户已购是否超出限购
            // （二）“慢sql，两条查询列表特别浪费时间”：索引 + 联表查询,`order`(user_id,order_status)+'order_item'(order_id,sku_id)
            /*Long targetSkuId = seckillGoods.getSkuId();
                // 2.1 先查询该用户 该商品订单 的orderId集合（订单状态：已支付成功，不是取消、超时关单、退款）
            List<Long> existOrderIds = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, userId)
                            .notIn(Order::getOrderStatus, Arrays.asList(4,5)))
                    .stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            int alreadyBuyNum = 0;
            if(!CollectionUtils.isEmpty(existOrderIds)){
                // 2.2 根据有效订单id + skuId，去order_item求和count
                alreadyBuyNum = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                                .in(OrderItem::getOrderId, existOrderIds)
                                .eq(OrderItem::getSkuId, targetSkuId))
                        .stream()
                        .mapToInt(OrderItem::getCount)
                        .sum();
            }*/
            Integer alreadyBuyNum = orderItemMapper.getUserBuySumBySkuId(userId, seckillGoods.getSkuId());//不要从Securityutils拿userId，因为MQ消费者是独立新线程，没有http请求
            alreadyBuyNum = alreadyBuyNum == null ? 0 : alreadyBuyNum;//未查到默认为0

            // 2.3 校验限购：已购 + 本次购买数量 > 限购数量，则抛出异常
            if(alreadyBuyNum + buyCount > seckillGoods.getLimitPerson()){
                // 原子回滚缓存（用户已购、商品库存）
                List<Object> keys = List.of(SECKILL_STOCK_PREFIX + seckillGoodsId, SECKILL_USER_BUY_PREFIX + seckillGoodsId + ":" + userId);
                String luaScript = """
                        local stockKey = KEYS[1]
                        local buyKey = KEYS[2]
                        local buyCount = tonumber(ARGV[1])
                        redis.call('INCRBY', stockKey, buyCount)
                        redis.call('DECRBY', buyKey, buyCount)
                        return 1
                        """;
                Long luaResult = redissonClient.getScript().eval(
                        RScript.Mode.READ_WRITE,
                        luaScript,
                        RScript.ReturnType.LONG,
                        keys,
                        buyCount
                );
                log.info("用户id：{}购买商品id：{},已购{},本次购买{},超过限额{}",userId,seckillGoodsId,alreadyBuyNum,buyCount,seckillGoods.getLimitPerson());
                throw new BusinessException(ResultCode.SECKILL_OUT_USER_LIMIT);
            }


            // 3、校验是否充足 可用秒杀库存
            if(seckillGoods.getSeckillStock() < buyCount){
                throw new BusinessException(ResultCode.SECKILL_STOCK_SHORTAGE);
            }

            // 4、预占库存（秒杀下单）
            int row = seckillGoodsMapper.lockSeckillGoodsStock(seckillGoodsId, buyCount, seckillGoods.getVersion());
            if(row == 0){
                throw new BusinessException(ResultCode.STOCK_OPTIMISTIC_LOCK_FAIL);
            }

            // 5、订单主表
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

            // 6、订单子项
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

            // 7、库存流水
            saveStockLog(
                    StockLogSeckillDTO.builder()
                        .beforeStock(seckillGoods)
                        .afterStock(baseMapper.selectById(seckillGoodsId))
                        .skuId(seckillGoods.getSkuId())
                        .orderId(orderId)
                        .orderSn(orderSn)
                        .seckillGoodsId(seckillGoodsId)
                        .changeType(StockConstant.CHANGE_TYPE_SECKILL_LOCK)
                        .changeNum(buyCount)
                        .remark("秒杀下单预占库存，订单号：" + orderSn)
                        .build()
            );

            // 8、发送30分钟超时关单消息
            orderDelayProducer.sendOrderDelayMsg(orderId);
            log.info("秒杀下单，MQ消费成功,msgId:{},orderId:{},orderSn:{}",msgDTO.getMsgId(),orderId,orderSn);

        }catch (Exception e){
            log.error("秒杀消费异常 msgId:{}",msgDTO.getMsgId(),e);
            throw new RuntimeException(e);
        }finally {
            if(getLock && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * Sentinel 限流、热点参数拦截
     * 触发：QPS超限、热点参数限流、系统保护规则触发
     * 写法：在同一个类里，限流和降级：参数和返回值和原方法一致，最后多一个 BlockException
     */
    public void seckillBlockHandler(Long seckillGoodsId,SeckillOrderCreateDTO dto, BlockException ex){
        log.warn("秒杀接口被Sentinel限流/热点拦截", ex);
        // 抛自定义业务异常，返回给前端：秒杀请求过多，请稍后重试
        throw new BusinessException(ResultCode.SECKILL_FLOW_LIMIT);
    }

    /**
     * 业务异常降级 fallback
     * 业务代码抛异常时触发降级（例如依赖服务超时）
     */
    public void seckillFallbackHandler(Long seckillGoodsId,SeckillOrderCreateDTO dto, Throwable e){
        log.error("秒杀业务触发降级", e);
        throw new BusinessException(ResultCode.SECKILL_DEGRADE);
    }

    // 2、 取消订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSeckillOrder(Long orderId) {
        //1、校验：登录
        Long userId = UserUtils.validUserLogin().getId();

        //2、校验：秒杀订单存在
        Order order = validSeckillOrderExist(orderId);

        //3、校验 是否 ① 是待支付订单 （只能取消待支付订单，已取消、已支付...全都禁止） ② 是否已取消（禁止重复取消）
        orderService.validOrderStatusIsNoPay(order.getOrderStatus());

        //4 校验 是否 是自己的订单
        orderService.validOrderSelf(order,userId);

        //5、6、7、8、9、 更新订单主表 删除订单子项 取消订单回滚锁定库存 保存秒杀库存流水  用户已购缓存回滚
        closeOrder(order,
                "用户"+userId+"手动取消订单"+order.getOrderSn()+"成功");

        log.info("用户手动取消秒杀订单成功 userId:{},orderSn:{}",userId,order.getOrderSn());
    }

    // 3、 30分钟超时关单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSeckillOrderTimeout(Long orderId) {

        //1、校验：秒杀订单存在
        Order order = validSeckillOrderExist(orderId);

        // 2 校验订单是否可取消（订单超时关闭）（包括各种状态处理）
        orderService.validOrderIsCancel(order);

        //不需要校验：只能操作自己的订单，关闭是后台行为，不是用户行为，与用户登录无关

        //5、6、7、8、9、 更新订单主表 删除订单子项 取消订单回滚锁定库存 保存秒杀库存流水  用户已购缓存回滚
        closeOrder(order, "秒杀订单超时自动关闭成功 orderSn:"+order.getOrderSn());

        log.info("秒杀订单超时自动关闭成功 orderId:{}, orderSn:{}",order.getId(),order.getOrderSn());
    }

        // 关闭订单（数据库）
    private void closeOrder(Order order,String remark){
        //  5 更新订单主表 订单状态、取消时间
        Long orderId = order.getId();
        orderService.setOrderCancelStatus(orderId);

        //  6 删除订单子项
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        //  7 取消订单回滚锁定库存
        orderItems.forEach(item -> {
            // 先查旧数据
            SeckillGoods oldGoods = seckillGoodsMapper.selectOne(new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getSkuId, item.getSkuId()));

            //  取消订单回滚锁定库存
            int i = baseMapper.rollbackLockStock(oldGoods.getId(), item.getCount(), oldGoods.getVersion());
            if (i == 0) {
                throw new BusinessException(ResultCode.STOCK_SECKILL_ROLLBACK_FAIL);
            }
            //  8 保存秒杀库存流水
            saveStockLog(
                    StockLogSeckillDTO.builder()
                        .beforeStock(oldGoods)
                        .afterStock(seckillGoodsMapper.selectById(oldGoods.getId()))
                        .skuId(oldGoods.getSkuId())
                        .orderId(orderId)
                        .orderSn(order.getOrderSn())
                        .seckillGoodsId(oldGoods.getId())
                        .changeType(StockConstant.CHANGE_TYPE_SECKILL_CANCEL_ROLLBACK)
                        .changeNum(item.getCount())
                        .remark(remark)
                        .build());

            // 9 用户已购缓存回滚
            redissonClient.getBucket(SECKILL_USER_BUY_PREFIX + oldGoods.getId() + ":" + order.getUserId()).delete();
        });
    }

//    /**
//     * 秒杀订单申请退款（模仿OrderRefundServiceImpl applyRefund）
//     * 前提：订单必须是【已支付】状态
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void applySeckillRefund(OrderRefundApplyDTO dto) {
//        //1、校验登录
//        Long userId = UserUtils.validUserLogin().getId();
//
//        //2、校验秒杀订单
//        Order order = validSeckillOrderExist(dto.getOrderId());
//
//        //3、只能退自己订单
//        if(!Objects.equals(order.getUserId(),userId)){
//            throw new BusinessException(ResultCode.ORDER_NOT_SLEF_REFUND_OPERATE_FORBID);
//        }
//
//        //4、状态校验：秒杀订单只有【已支付】可以申请退款
//        if(!Objects.equals(order.getOrderStatus(),OrderConstant.ORDER_HAVING_PAY)){
//            throw new BusinessException(ResultCode.ORDER_REFUND_OPERATE_FORBID,"秒杀订单仅已支付状态支持申请退款");
//        }
//
//        //5、校验是否已经存在退款记录
//        Long count = baseMapper.selectCount(new LambdaQueryWrapper<OrderRefund>()
//                .eq(OrderRefund::getOrderId, dto.getOrderId()));
//        if(count >0){
//            throw new BusinessException(ResultCode.ORDER_REFUND_EXIST);
//        }
//
//        //6、新增退款记录
//        OrderRefund refund = OrderRefund.builder()
//                .orderId(dto.getOrderId())
//                .refundSn(UUID.fastUUID().toString(true))
//                .refundAmount(order.getPayAmount())
//                .refundStatus(OrderConstant.ORDER_DOING_REFUND)
//                .refundReason(dto.getRefundReason())
//                .build();
//        //注意：你这里需要注入OrderRefundMapper
//        orderRefundMapper.insert(refund);
//
//        //7、更新订单状态为退款中
//        Order updateOrder = Order.builder()
//                .id(dto.getOrderId())
//                .orderStatus(OrderConstant.ORDER_DOING_REFUND)
//                .build();
//        orderMapper.updateById(updateOrder);
//
//        log.info("用户{}发起秒杀订单退款申请 orderSn:{}",userId,order.getOrderSn());
//    }
//
//
//    /**
//     * 【退款成功回调接口】秒杀退款真正回滚库存
//     * 支付组件回调过来，此时退款已经真实打给用户
//     * seckill_sold -= count， seckill_stock += count
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void seckillRefundSuccessCallback(Long orderId) {
//        //1、查询订单
//        Order order = validSeckillOrderExist(orderId);
//
//        //2、校验状态必须是退款中
//        if(!Objects.equals(order.getOrderStatus(),OrderConstant.ORDER_DOING_REFUND)){
//            throw new BusinessException(ResultCode.ORDER_REFUND_STATUS_ERROR);
//        }
//
//        //3、获取订单项，拿到skuId、购买数量
//        OrderItem orderItem = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
//                .eq(OrderItem::getOrderId,orderId));
//        if(orderItem == null){
//            throw new BusinessException(ResultCode.ORDER_ITEM_NOT_EXIST);
//        }
//
//        //4、查询秒杀商品
//        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(new LambdaQueryWrapper<SeckillGoods>()
//                .eq(SeckillGoods::getSkuId, orderItem.getSkuId()));
//
//        //5、乐观锁退款回滚：seckill_sold - count，seckill_stock + count
//        int updateRow = seckillGoodsMapper.rollbackStockByRefund(seckillGoods.getId(), orderItem.getCount(), seckillGoods.getVersion());
//        if(updateRow == 0){
//            throw new BusinessException(ResultCode.STOCK_OPTIMISTIC_LOCK_FAIL,"秒杀退款回调，库存回滚失败");
//        }
//
//        //6、保存库存流水
//        SeckillGoods after = seckillGoodsMapper.selectById(seckillGoods.getId());
//        saveStockLog(StockLogSeckillDTO.builder()
//                .beforeStock(seckillGoods)
//                .afterStock(after)
//                .orderId(orderId)
//                .orderSn(order.getOrderSn())
//                .seckillGoodsId(seckillGoods.getId())
//                .changeType(StockConstant.CHANGE_TYPE_REFUND_ROLLBACK)
//                .changeNum(orderItem.getCount())
//                .remark("秒杀订单退款成功，归还秒杀可售库存 orderSn:"+order.getOrderSn())
//                .build());
//
//        //7、更新订单状态为退款完成
//        Order updateOrder = Order.builder()
//                .id(orderId)
//                .orderStatus(OrderConstant.ORDER_HAVING_REFUND)
//                .build();
//        orderMapper.updateById(updateOrder);
//
//        log.info("秒杀订单退款回调完成 orderSn:{}",order.getOrderSn());
//    }

    // 校验秒杀订单存在
    @Override
    @SentinelResource(value="getSeckillGoodsResource", fallback = "getSeckillGoodsFallback" ) // 参数二：降级方法
    public Order validSeckillOrderExist(Long seckilLId){
        Order order = orderMapper.selectById(seckilLId);
        if(order == null){
            throw new BusinessException(ResultCode.SECKILL_ORDER_NOT_EXIST);
        }
        return order;
    }

    // 降级方法，商品服务查询异常熔断，返回降级结果
    public SeckillGoods getSeckillGoodsFallback(Long seckillGoodsId, Throwable e){
        log.error("查询秒杀商品触发熔断降级",e);
        throw new BusinessException(SECKILL_GOODS_DEGRADE);
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

        int insert = stockLogMapper.insert(stockLog);
        if(insert == 0){
            throw new BusinessException(ResultCode.STOCK_LOG_SAVE_FAIL);
        }
    }

}



