package com.zh.hengyi.application.service.order;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.order.OrderItemMapper;
import com.zh.hengyi.application.mapper.order.OrderMapper;
import com.zh.hengyi.application.mapper.stock.StockMapper;
import com.zh.hengyi.application.model.dto.order.OrderCreateDTO;
import com.zh.hengyi.application.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.application.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.application.model.dto.stock.StockDeductDTO;
import com.zh.hengyi.application.model.dto.stock.StockRollbackDTO;
import com.zh.hengyi.application.model.entity.order.Order;
import com.zh.hengyi.application.model.entity.order.OrderItem;
import com.zh.hengyi.application.model.entity.order.OrderRefund;
import com.zh.hengyi.application.model.entity.product.ProductSku;
import com.zh.hengyi.application.model.entity.product.ProductSpu;
import com.zh.hengyi.application.model.vo.order.OrderDetailVO;
import com.zh.hengyi.application.model.vo.order.OrderItemVO;
import com.zh.hengyi.application.model.vo.order.OrderPageVO;
import com.zh.hengyi.application.model.vo.order.OrderRefundVO;
import com.zh.hengyi.application.service.cart.CartService;
import com.zh.hengyi.application.service.product.ProductSkuService;
import com.zh.hengyi.application.service.product.ProductSpuService;
import com.zh.hengyi.application.service.stock.StockService;
import com.zh.hengyi.common.constant.*;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.security.UserUtils;
import com.zh.hengyi.component.rabbitmq.order.OrderDelayProducer;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.constant.SeckillConstant.ORDER_STATUS_NORMAL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final ProductSkuService skuService;
    private final ProductSpuService spuService;
    private final OrderRefundService orderRefundService;
    private final OrderDelayProducer orderDelayProducer;
    private final StockService stockService;
    @Resource(name = "orderTaskExecutor")
    private Executor orderTaskExecutor;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private StockMapper stockMapper;

    // 1 购物车结算创建订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreateDTO dto) {
        // 1. 校验登录
        Long userId = UserUtils.validUserLogin().getId();

        // 2.1 校验购物车缓存、数据库是否都为空，都为空直接返回，只要数据库中有，回写缓存
        Map<String, Integer> allCart = cartService.getUserCartRMap().readAllMap();
        Map<String, Integer> allSelectStatus = cartService.getUserCartSelectRMap().readAllMap();
        allCart = cartService.validCartCacheExist(allCart,allSelectStatus);

        // 2.2 只筛选购物车选中的商品，且不为空
        Map<String, Integer> selectedCart = allCart.entrySet().stream()
                .filter(entry ->
                        Objects.equals(cartService.strToInt(allSelectStatus.getOrDefault(entry.getKey(), CartConstant.CART_NOT_SELECT)), CartConstant.CART_SELECT))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (CollUtil.isEmpty(selectedCart)) {
            throw new BusinessException(ResultCode.CART_NO_SELECT);
        }

        // 3. 批量查询SKU、SPU信息    先拿到 skuIds和 spuIds，再分别获取列表
        List<Long> skuIds = selectedCart.keySet().stream().map(Long::valueOf).collect(Collectors.toList());
        List<ProductSku> skuList = skuService.listByIds(skuIds);
        Map<Long, ProductSku> skuMap = skuList.stream().collect(Collectors.toMap(ProductSku::getId, s->s));
        List<Long> spuIds = skuList.stream().map(ProductSku::getSpuId).distinct().collect(Collectors.toList());
        Map<Long, ProductSpu> spuMap = spuService.listByIds(spuIds).stream().collect(Collectors.toMap(ProductSpu::getId, s->s));

        // 4. 构建订单子项（每个商品）
        BigDecimal totalAmount = BigDecimal.ZERO; //计算订单主表总金额
        List<OrderItem> orderItemList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedCart.entrySet()) {
            // 获取购物车缓存下商品规格id，数量，以及库中skuId,spuId
            Long skuId = Long.valueOf(entry.getKey());
            Integer count = cartService.strToInt(entry.getValue());
            ProductSku sku = skuMap.get(skuId);
            ProductSpu spu = spuMap.get(sku.getSpuId());
            // 计算每个商品的总金额
            BigDecimal itemTotal = sku.getPrice().multiply(new BigDecimal(count));
            totalAmount  = totalAmount.add(itemTotal);

            orderItemList.add(OrderItem.builder()
                        .spuId(sku.getSpuId())
                        .skuId(skuId)
                        .spuName(spu.getSpuName())
                        .skuSpec(sku.getSkuSpec())
                        .price(sku.getPrice())
                        .count(count)
                        .totalPrice(itemTotal)
                        .build());
        }

        // 5. 构建订单主表
        Order order = Order.builder()
                           .orderSn(UUID.fastUUID().toString(true))
                           .orderType(ORDER_STATUS_NORMAL)
                           .userId(userId)
                           .totalAmount(totalAmount)
                           .payAmount(totalAmount)// 暂不做优惠券抵扣
                           .orderStatus(OrderConstant.ORDER_NO_PAY)
                           .remark(dto.getRemark())
                           .build();

        // 6. 批量保存订单主表、订单子表（利用订单主表的回填订单id）
        orderMapper.insert(order);
        Long orderId = order.getId();
        orderItemList.forEach(item -> item.setOrderId(orderId));
        orderItemService.saveBatch(orderItemList);

        // 7. 💎 清理购物车已勾选商品（缓存、选中缓存、数据库双删）
        cartService.removeSelected(selectedCart.keySet().stream().collect(Collectors.toList()));
        log.info("用户{}下单成功，订单号：{}，清理购物车选中商品缓存", userId, order.getOrderSn());

        // 8.库存下单预占
        StockDeductDTO stockDeDTO = new StockDeductDTO();
        stockDeDTO.setOrderId(orderId);
        stockDeDTO.setOrderSn(order.getOrderSn());
        stockDeDTO.setSkuNumList(orderItemList.stream().map(
                    item->{
                        StockDeductDTO.SkuNumDTO skuNumDTO = new StockDeductDTO.SkuNumDTO();
                        skuNumDTO.setSkuId(item.getSkuId());
                        skuNumDTO.setCount(item.getCount());
                        return skuNumDTO;
                    }
                ).collect(Collectors.toList()));
        stockService.lockStock(stockDeDTO);

        // 10.发送延迟消息，30分钟超时自动关单 (放在最后，避免事务回滚，但消息已经发出去)
        orderDelayProducer.sendOrderDelayMsg(orderId);
    }

    // 2 取消订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        // 1 校验 登录
        Long userId = UserUtils.validUserLogin().getId();

        // 2 校验 订单存在
        Order order = validOrderExist(orderId);

        // 校验 是否 ① 是待支付订单 （只能取消待支付订单，已取消、已支付...全都禁止） ② 是否已取消（禁止重复取消）
        validOrderStatusIsNoPay(order.getOrderStatus());

        // 4 校验 是否 是自己的订单
        validOrderSelf(order,userId);

        // 5、6、 7. 8 更新订单主表状态，删除订单子表 取消订单归还锁定库存 写入库存流水
        closeOrder(orderId,
                "用户"+userId+"手动取消订单"+order.getOrderSn()+"成功");
        log.info("用户{}手动取消订单{}成功", userId, order.getOrderSn());

        // ❗️❗️❗️TODO：重新放回购物车

    }

    // 3 30分钟未支付自动关闭订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOrderByTimeout(Long orderId) {
        // 1. 校验 订单存在
        Order order = validOrderExist(orderId);

        // 2 校验订单是否可取消（订单超时关闭）（包括各种状态处理）
        validOrderIsCancel(order);

        // 不需要校验：只能操作自己的订单，关闭是后台行为，不是用户行为，与用户登录无关

        // 3、4、5、6
        closeOrder(orderId,
                "订单超时未支付，系统自动关闭成功,订单号order_sn"+order.getOrderSn());
        log.info("订单{}超时未支付，系统自动关闭成功", order.getOrderSn());

        // ❗️❗️❗️TODO：重新放回购物车

    }

    // 关闭订单（数据库）
    private void closeOrder(Long orderId, String remark) {
        //  更新订单主表 订单状态、取消时间
        setOrderCancelStatus(orderId);

        //  删除订单子表、
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));


        //  取消订单回滚锁定库存、写入库存流水
        StockRollbackDTO dto = new StockRollbackDTO();
        dto.setOrderId(orderId);
        dto.setChangeType(StockConstant.CHANGE_TYPE_CANCEL_ROLLBACK);
        dto.setRemark(remark);
        dto.setSkuNumList(orderItems.stream().map(
                item->{
                        StockRollbackDTO.SkuNumDTO skuNumDTO = new StockRollbackDTO.SkuNumDTO();
                        skuNumDTO.setSkuId(item.getSkuId());
                        skuNumDTO.setCount(item.getCount());
                        return skuNumDTO;
                    }
                ).collect(Collectors.toList()));
        stockService.rollbackCancelStock(dto);
    }


    // 4 查询我的订单分页
    @Override
    public IPage<OrderPageVO> getMyOrderPage(OrderQueryUserDTO dto) {
        //校验登录
        Long userId = UserUtils.validUserLogin().getId();

        // 仅查询自己订单
        IPage<Order> orderPage = baseMapper.selectUserOrderPage(new Page<>(dto.getPageNum(), dto.getPageSize()), userId, dto);
        return orderToOrderVO(orderPage);

    }

    // 5 查询后台订单分页
    @Override
    public IPage<OrderPageVO> getAdminOrderPage(OrderQueryAdminDTO dto) {
        //校验登录
        UserUtils.validUserLogin();

        // 查询商品订单分页列表
        IPage<Order> orderPage = baseMapper.selectAdminOrderPage(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
        return orderToOrderVO(orderPage);
    }

    // 6 订单详情（CompletableFuture 并行查询 提升查询速度）
    /*
    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        // 1 校验登录
        UserUtils.validUserLogin();

        // 2 校验订单存在
        Order order = validOrderExist(orderId);

        // 3 返回商品详情VO
        OrderDetailVO vo = BeanUtil.copyProperties(order, OrderDetailVO.class);
        vo.setorderItemList(BeanUtil.copyToList(orderItemService.getByOrderId(orderId), OrderItemVO.class));

        // 3.2 查询退款记录
        OrderRefund orderRefund = orderRefundService.getOne(new LambdaQueryWrapper<OrderRefund>().eq(OrderRefund::getOrderId, orderId));
        if(orderRefund != null){
            vo.setRefund(BeanUtil.copyProperties(orderRefund, OrderRefundVO.class));
        }

        return vo;
    }
    */
    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        // 校验登录
        UserUtils.validUserLogin();

        // 并行三个任务
        CompletableFuture<Order> orderFuture = CompletableFuture.supplyAsync(() -> validOrderExist(orderId), orderTaskExecutor);
        CompletableFuture<List<OrderItem>> itemFuture = CompletableFuture.supplyAsync(() -> orderItemService.getByOrderId(orderId), orderTaskExecutor);
        CompletableFuture<OrderRefund> refundFuture = CompletableFuture.supplyAsync(() -> orderRefundService.getOne(new LambdaQueryWrapper<OrderRefund>().eq(OrderRefund::getOrderId, orderId)), orderTaskExecutor);

        // 等待全部任务完成
        CompletableFuture.allOf(orderFuture, itemFuture, refundFuture).join();

        // 获取结果
        Order order = orderFuture.join();
        List<OrderItem> orderItemList = itemFuture.join();
        OrderRefund orderRefund = refundFuture.join();

        // 返回VO
        OrderDetailVO vo = BeanUtil.copyProperties(order, OrderDetailVO.class);
        vo.setItemList(BeanUtil.copyToList(orderItemList, OrderItemVO.class));
        if(orderRefund != null){
            vo.setRefund(BeanUtil.copyProperties(orderRefund, OrderRefundVO.class)) ;
        }
        return vo;
    }

    @Override
    public Order validOrderExist(Long orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PAY_ORDER_NOT_EXIST);
        }
        return order;
    }

    @Override
    public void validOrderSelf(Order order,Long userId){
        if(!Objects.equals(order.getUserId(), userId)){
            throw new BusinessException(ResultCode.ORDER_NOT_SLEF_OPERATE_FORBID);
        }
    }

    // 校验 是否 ① 是待支付订单 （只能取消待支付订单，已取消、已支付...全都禁止） ② 是否已取消
    @Override
    public void validOrderStatusIsNoPay(Integer orderStatus){
        if(!Objects.equals(orderStatus, OrderConstant.ORDER_NO_PAY)){
            if(orderStatus.equals(OrderConstant.ORDER_HAVING_CANCEL)){
                throw new BusinessException(ResultCode.ORDER_CANCEL_UNIQUE_FORBID);
            }
            throw new BusinessException(ResultCode.ORDER_CANCEL_FORBID);
        }
    }

    // 校验：订单是否可取消（订单超时关闭，包括各种状态处理）
    @Override
    public void validOrderIsCancel(Order order){
        // 订单为待支付，直接返回
        if(Objects.equals(order.getOrderStatus(), OrderConstant.ORDER_NO_PAY)){
            return;
        }
        // 订单为非待支付，只打印日志
        if (Objects.equals(order.getOrderStatus(), OrderConstant.ORDER_HAVING_CANCEL)){
            log.info("订单orderId:{} 已取消，禁止重复取消",order.getId());
            return;
        }
        if(OrderConstant.ORDER_HAVING_PAY.equals(order.getOrderStatus())
                || OrderConstant.ORDER_HAVING_SEND.equals(order.getOrderStatus())
                || OrderConstant.ORDER_HAVING_DONE.equals(order.getOrderStatus())
                || OrderConstant.ORDER_DOING_REFUND.equals(order.getOrderStatus())){
            log.info("订单已经处理完毕，订单状态 orderStatus:{}，跳过 orderId:{}",order.getOrderStatus(),order.getId());
            return;
        }
    };

    public IPage<OrderPageVO> orderToOrderVO(IPage<Order> orderPage) {
        IPage<OrderPageVO> vo = new Page<>();
        BeanUtils.copyProperties(orderPage, vo,"records");
        // 单独转换records order->orderVO
        vo.setRecords(orderPage.getRecords().stream()
                .map(order -> BeanUtil.copyProperties(order, OrderPageVO.class))
                .collect(Collectors.toList()));
        return vo;
    }

    // 更新订单主表为取消状态
    @Override
    public void setOrderCancelStatus(Long orderId){
        // 设置订单为已取消订单
        Order order = Order.builder()
                .id(orderId)
                .orderStatus(OrderConstant.ORDER_HAVING_CANCEL)
                .cancelTime(LocalDateTime.now())
                .build();
        baseMapper.updateById(order);
    }

}