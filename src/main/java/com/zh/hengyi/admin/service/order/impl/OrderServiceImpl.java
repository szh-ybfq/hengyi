package com.zh.hengyi.admin.service.order.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.model.dto.order.OrderCreateDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.admin.model.entity.cart.Cart;
import com.zh.hengyi.admin.model.entity.order.Order;
import com.zh.hengyi.admin.model.entity.order.OrderItem;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.vo.cart.CartTotalVO;
import com.zh.hengyi.admin.model.vo.cart.CartVO;
import com.zh.hengyi.admin.model.vo.order.OrderDetailVO;
import com.zh.hengyi.admin.model.vo.order.OrderItemVO;
import com.zh.hengyi.admin.model.vo.order.OrderPageVO;
import com.zh.hengyi.admin.service.cart.CartService;
import com.zh.hengyi.admin.service.order.OrderItemService;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.common.constant.OrderConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemService orderItemService;
//    private final OrderRefundService refundService;
    private final CartService cartService;
    private final RedissonClient redissonClient;
    private final ProductSkuService skuService;
    private final ProductSpuService spuService;

    // 购物车结算创建订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreateDTO dto) {
        // 1. 校验登录
        cartService.validUserLogin();

        // 2.1 校验购物车缓存、数据库是否都为空，都为空直接返回，只有数据库中有，回写缓存
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        RMap<String, Integer> cartRMap = cartService.getUserCartRMap();
        RMap<String, Integer> selectRMap = cartService.getUserCartSelectRMap();
        Map<String, Integer> allCartMap = cartRMap.readAllMap();
        Map<String, Integer> allSelectMap = selectRMap.readAllMap();
        if (CollUtil.isEmpty(allCartMap)) {
            // 缓存空，再查询数据库
            List<CartVO> cartVOList = cartService.getCartList().getCartList();
            List<Cart> cartList =new ArrayList<>();
            cartVOList.stream().forEach(cartVO -> cartList.add(BeanUtil.copyProperties(cartVO, Cart.class)));
            // 数据库也没有购物车，直接抛异常
            if (CollUtil.isEmpty(cartList)) {
                throw new BusinessException(ResultCode.CART_EMPTY);
            }
            // 数据库存在购物车，重新加载到Redis缓存
            cartService.reloadCartCache(cartList);
            // 重新回写，读取最新缓存
            allCartMap = cartRMap.readAllMap();
        }

        // 2.2 只筛选购物车选中的商品，且不为空
        Map<String, Integer> selectedCart = allCartMap.entrySet().stream()
                .filter(entry ->
                        Objects.equals(cartService.strToInt(allSelectMap.getOrDefault(entry.getKey(), 0)), 1))
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

        // 4. 构建订单项（每个商品）
        BigDecimal totalAmount = BigDecimal.ZERO; //计算订单主表总金额
        List<OrderItem> itemList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedCart.entrySet()) {
            // 获取购物车缓存下商品规格id，数量，以及库中skuId,spuId
            Long skuId = Long.valueOf(entry.getKey());
            Integer count = cartService.strToInt(entry.getValue());
            ProductSku sku = skuMap.get(skuId);
            ProductSpu spu = spuMap.get(sku.getSpuId());
            // 计算每个商品的总金额
            BigDecimal itemTotal = sku.getPrice().multiply(new BigDecimal(count));
            totalAmount  = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .spuId(sku.getSpuId())
                    .skuId(skuId)
                    .spuName(spu.getSpuName())
                    .skuSpec(sku.getSkuSpec())
                    .price(sku.getPrice())
                    .count(count)
                    .totalPrice(itemTotal)
                    .build();
            itemList.add(orderItem);
        }

        // 5. 构建订单主表
        Order order = Order.builder()
                .orderSn(UUID.fastUUID().toString(true))
                .userId(userId)
                .totalAmount(totalAmount)
                .payAmount(totalAmount)// 暂不做优惠券抵扣
                .orderStatus(OrderConstant.ORDER_NO_PAY)
                .remark(dto.getRemark())
                .build();
        orderMapper.insert(order);

        // 6. 批量保存订单项，填充订单id（利用订单主表的回填id）
        itemList.forEach(item -> item.setOrderId(order.getId()));
        orderItemService.saveBatch(itemList);

        // 7. 💎 清理购物车已勾选商品（缓存、选中缓存、数据库双删）
        cartService.removeSelected(selectedCart.keySet().stream().collect(Collectors.toList()));

        log.info("用户{}下单成功，订单号：{}，清理购物车选中商品缓存", userId, order.getOrderSn());

        // TODO 后续迭代：1. 库存扣减 2. MQ延迟队列30分钟未支付自动关单
    }

    // 查询我的订单分页
    @Override
    public IPage<OrderPageVO> getMyOrderPage(OrderQueryUserDTO dto) {
        //校验登录
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        if (userId==null) {
            throw new BusinessException(ResultCode.LOGIN_NOT_EXIST);
        }

        // 仅查询自己订单
        IPage<Order> orderPage = baseMapper.selectUserOrderPage(new Page<>(dto.getPageNum(), dto.getPageSize()), userId, dto);
        return orderToOrderVO(orderPage);

    }

    // 查询后台订单分页
    @Override
    public IPage<OrderPageVO> getAdminOrderPage(OrderQueryAdminDTO dto) {
        //校验登录
        cartService.validUserLogin();

        // 查询商品订单分页列表
        IPage<Order> orderPage = baseMapper.selectAdminOrderPage(new Page<>(dto.getPageNum(), dto.getPageSize()), dto);
        return orderToOrderVO(orderPage);
    }

    // 订单详情
    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        // 1 校验登录
        cartService.validUserLogin();

        // 2 校验订单存在
        Order order = validOrderExist(orderId);

        // 3 返回商品详情VO
        OrderDetailVO vo = BeanUtil.copyProperties(order, OrderDetailVO.class);
        vo.setItemList(BeanUtil.copyToList(orderItemService.getByOrderId(orderId), OrderItemVO.class));

        // 3.2 查询退款记录
//        OrderRefund refund = refundService.getOne(new LambdaQueryWrapper<OrderRefund>().eq(OrderRefund::getOrderId, orderId));
//        if(refund != null){
//            vo.setRefund(BeanUtil.copyProperties(order, OrderRefundVO.class));
//        }

        return vo;
    }

    // 取消未支付订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        // 1 校验登录
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        if (userId==null) {
            throw new BusinessException(ResultCode.LOGIN_NOT_EXIST);
        }

        // 2 校验订单存在
        Order order = validOrderExist(orderId);

        // 3 校验 只能取消待支付订单
        if(!Objects.equals(order.getOrderStatus(), OrderConstant.ORDER_NO_PAY)){
            throw new BusinessException(ResultCode.ORDER_CANCEL_FORBID);
        }

        // 4 校验 只能取消自己的订单
        if(!Objects.equals(order.getUserId(), userId)){
            throw new BusinessException(ResultCode.ORDER_NOT_SLEF_OPERATE_FORBID);
        }

        // 5 更新订单状态、取消时间
        Order updateOrder = Order.builder()
                .id(orderId)
                .orderStatus(OrderConstant.ORDER_HAVING_CANCEL)
                .cancelTime(LocalDateTime.now())
                .build();
        baseMapper.updateById(updateOrder);
        log.info("用户{}取消订单{}成功", userId, order.getOrderSn());
        // ❗️❗️❗️ TODO：重新放回购物车
        // TODO 迭代：取消订单回滚商品库存
    }

    // 校验订单是否存在
    public Order validOrderExist(Long orderId) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    // order -> orderVO
    public IPage<OrderPageVO> orderToOrderVO(IPage<Order> orderPage) {
        IPage<OrderPageVO> vo = new Page<>();
        BeanUtils.copyProperties(orderPage, vo,"records");
        // 单独转换records order->orderVO
        vo.setRecords(orderPage.getRecords().stream()
                .map(order -> BeanUtil.copyProperties(order, OrderPageVO.class))
                .collect(Collectors.toList()));
        return vo;
    }
}