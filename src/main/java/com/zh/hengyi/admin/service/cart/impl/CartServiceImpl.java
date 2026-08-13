package com.zh.hengyi.admin.service.cart.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.cart.CartMapper;
import com.zh.hengyi.admin.model.dto.cart.CartAddDTO;
import com.zh.hengyi.admin.model.dto.cart.CartSelectDTO;
import com.zh.hengyi.admin.model.dto.cart.CartUpdateCountDTO;
import com.zh.hengyi.admin.model.entity.authority.User;
import com.zh.hengyi.admin.model.entity.cart.Cart;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.vo.cart.CartCalTotalVO;
import com.zh.hengyi.admin.model.vo.cart.CartTotalVO;
import com.zh.hengyi.admin.model.vo.cart.CartVO;
import com.zh.hengyi.admin.service.cart.CartService;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.common.constant.CartConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.common.utils.security.UserUtils;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HENGGE
 * @description 表 cart(购物车表) 实现
 * @createDate 2026-08-11 08:04:12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final CartMapper cartMapper;

    // 替换 RedisTemplate，统一使用RedissonClient
    private final RedissonClient redissonClient;
    private final  ProductSkuService productSkuService;
    private final ProductSpuService productSpuService;

    // Redis购物车key前缀
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String CART_SElECT_KEY_PREFIX = "cart:user:select:";
    private static final Integer CART_TTL_DAYS = 30;

    // 获取当前登录用户购物车缓存  （RMap：Redisson 封装 Redis Hash 接口，支持原子操作，分布式锁，读写分离，readAllMap() 只读不抢占写线程）
    @Override
    public RMap<String, Integer> getUserCartRMap() {
        return redissonClient.getMap( CART_KEY_PREFIX +SecurityUtils.getLoginUser().getUser().getId());
    }

    // 获取当前用户购物车选中缓存
    @Override
    public RMap<String, Integer> getUserCartSelectRMap() {
        //先获取当前登录用户购物车选中缓存
        return redissonClient.getMap(CART_SElECT_KEY_PREFIX+SecurityUtils.getLoginUser().getUser().getId());
    }

    // 获取用户购物车选中选中缓存  方法重载
    @Override
    public RMap<String, Integer> getUserCartSelectRMap(Long userId) {
        return redissonClient.getMap(CART_SElECT_KEY_PREFIX + userId);
    }

    // 加入购物车
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(CartAddDTO dto) {
        // 登录校验
        UserUtils.validUserLogin();

        // Redisson原子累加hincrby，存在则数量+addCount，不存在自动新增，单条原子操作，无并发超量
        RMap<String, Integer> cartRMap = getUserCartRMap();
        Integer curCount = cartRMap.addAndGet(dto.getSkuId().toString(), dto.getCount());
        cartRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("写入购物车缓存成功");

        RMap<String, Integer> selectRMap = getUserCartSelectRMap(SecurityUtils.getLoginUser().getUser().getId());
        selectRMap.put(dto.getSkuId().toString(), CartConstant.CART_SELECT);
        selectRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("写入购物车选中缓存成功");

        // 更新数据库（数量和缓存同步，不单独再作count增减操作）
        // 报错修复 💎 直接用addAndGet()返回Integer类型     因为readAllMap() 底层是通用读取方法，返回 Map<K,Object>，因此实际上是value是String类型（Object转的）
        saveOrUpdateCart(dto.getSkuId(),curCount);
        log.info("添加数据库购物车记录成功");
    }

    // 修改购物车商品数量
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCount(CartUpdateCountDTO dto) {
        // 登录校验
        UserUtils.validUserLogin();
        // 检验判空
        validCartExist(dto.getSkuId());

        // 修改时直接覆盖原来数量，不用 Redisson原子累加
        RMap<String, Integer> cartMap = getUserCartRMap();
        cartMap.remove(dto.getSkuId().toString());
        cartMap.put(dto.getSkuId().toString(), dto.getCount());
        cartMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("修改购物车缓存成功");

        saveOrUpdateCart(dto.getSkuId(),dto.getCount());
        log.info("修改数据库购物车记录成功");
    }

    // 删除购物车单个商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCart(Long skuId) {
        // 登录校验
        UserUtils.validUserLogin();
        // 检验判空
        validCartExist(skuId);

        // 1 删除redis缓存里面RMap哈希对象 （1在2前 先清除脏缓存，避免并发查脏，增加、修改同理）
        RMap<String, Integer> cartRMap = getUserCartRMap();
        cartRMap.remove(skuId.toString());
        cartRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("清除单条购物车缓存成功");

        RMap<String, Integer> selectRMap = getUserCartSelectRMap(SecurityUtils.getLoginUser().getUser().getId());
        selectRMap.remove(skuId.toString());
        selectRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("清除单条购物车选中缓存成功");

        // 2 数据库删除
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, SecurityUtils.getLoginUser().getUser().getId()).eq(Cart::getSkuId, skuId));
        log.info("清除数据库购物车记录成功");
    }


    // 删除用户购物车已勾选商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSelected(List<String> removeSkuKeys) {
        // 校验是否之前勾选要下单商品
        if (CollUtil.isEmpty(removeSkuKeys)) {
            throw new BusinessException(ResultCode.CART_NO_SELECT);
        }

        //删除数据库购物车已勾选商品
        cartMapper.deleteSelected();

        // 删除购物车缓存、选中缓存
        RMap<String, Integer> cartRMap = getUserCartRMap();
        RMap<String, Integer> selectRMap = getUserCartSelectRMap();
        cartRMap.fastRemove(removeSkuKeys.toArray(new String[0]));
        selectRMap.fastRemove(removeSkuKeys.toArray(new String[0]));
        cartRMap.expire(Duration.ofDays(30));
        selectRMap.expire(Duration.ofDays(30));
    }


    /*
     * 修改购物车商品选中状态
     *    单条/多条 局部修改传skuId，skuIdList=[A,B]
     *    全选/清空 skuIdList=null selected=1 / skuIdList=null selected=0；
     */
    // 更新购物车选中缓存
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSelect(CartSelectDTO dto) {
        // 登录校验
        UserUtils.validUserLogin();

        // 先获取dto参数
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        Integer targetSelected = dto.getSelected();
        List<Long> skuIdList = dto.getSkuIdList();

        // 1. 更新缓存，先获取用户购物车选中RMap对象
        RMap<String, Integer> selectRMap = getUserCartSelectRMap(userId);
        if (CollUtil.isNotEmpty(skuIdList)) {
            // 局部勾选：批量put对应sku选中状态   (将 skuId列表转换为 Map对象，   key：String类型的SkuId     value：targetSelected   k叫啥无所谓)
            Map<String, Integer> tempMap = skuIdList.stream().collect(Collectors.toMap(String::valueOf, k -> targetSelected));
            selectRMap.putAll(tempMap);
            log.info("更新购物车单个或多个选中缓存成功");

        } else {
            // 全选/取消全选：清空原有数据，全量写入
            selectRMap.clear();
            // 先查该用户购物车所有sku,然后批量设为目标值
            List<Cart> allUserCart = cartMapper.selectList(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            Map<String, Integer> allSelectMap = allUserCart.stream().collect(Collectors.toMap(c -> c.getSkuId().toString(), c -> targetSelected));
            selectRMap.putAll(allSelectMap);
            log.info("更新购物车全选、取消全选缓存成功");
        }
        selectRMap.expire(Duration.ofDays(CART_TTL_DAYS));

        // 2. 更新数据库
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId);
        if (CollUtil.isNotEmpty(skuIdList)) {
            wrapper.in(Cart::getSkuId, skuIdList);
        }
        Cart entity = new Cart();
        entity.setSelected(targetSelected);
        cartMapper.update(entity, wrapper);
        log.info("更新数据库购物车选中状态成功");
    }

    // 查询当前用户完整购物车
    @Override
    public CartTotalVO getCartList() {
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        // 1. 先获取redis用户购物车列表RMap哈希表，再获取redis全量数据
        Map<String, Integer> allCartItem = getUserCartRMap().readAllMap();
        // 检验判空
        if (CollUtil.isEmpty(allCartItem)) {
            return new CartTotalVO();
        }

        // 2.1 获取数据库所有skuId（避免循环查库）,再批量查询sku详情（价格、规格、图片、库存、spu名称、上下架状态）         keySet():只要key这个集合        str——long
        List<Long> skuIdList = allCartItem.keySet().stream().map(Long::valueOf).collect(Collectors.toList());
        List<ProductSku> skuList = productSkuService.listByIds(skuIdList);
        Map<Long, ProductSku> skuInfoMap = skuList.stream().collect(Collectors.toMap(ProductSku::getId, Function.identity()));

        // 2.2 对sku列表的spuId并去重，再批量查spu表，构建spuName集合
        List<Long> spuIdList = skuList.stream().map(ProductSku::getSpuId).distinct().collect(Collectors.toList());
        Map<Long, String> spuNameMap = productSpuService.listByIds(spuIdList).stream().collect(Collectors.toMap(ProductSpu::getId, ProductSpu::getSpuName));

        // 3. 再获取redis购物车选中状态
        RMap<String, Integer> cartSelectMap = redissonClient.getMap(CART_SElECT_KEY_PREFIX+ userId);
        Map<String, Integer> selectMap = cartSelectMap.readAllMap();

        // 4. 组装每一条购物车CartVO
        List<CartVO> cartVOList = allCartItem.entrySet().stream()//转为键值对集合
                .filter(entry -> skuInfoMap.containsKey(Long.valueOf(entry.getKey()))) // 过滤已删除sku
                .map(entry -> {
                    //获取redis购物车列表集合中元素的键值
                    String skuKey = entry.getKey();
                    Long skuId = Long.valueOf(skuKey);
                    Integer buyCount = strToInt(entry.getValue());
                    //获取单条sku信息
                    ProductSku skuInfo = skuInfoMap.get(skuId);

                    CartVO vo = new CartVO();
                    vo.setUserId(userId);
                    vo.setSkuId(skuId);
                    vo.setSpuName(spuNameMap.get(skuInfo.getSpuId()));
                    vo.setSkuSpec(skuInfo.getSkuSpec());
                    //vo.setPicUrl(entity.getPicUrl());
                    vo.setPrice(skuInfo.getPrice());
                    vo.setCount(buyCount);
                    vo.setStock(skuInfo.getStock());
                    // 小计 = 单价 * 购买数量
                    vo.setSubTotal(skuInfo.getPrice().multiply(new BigDecimal(buyCount)));
                    // 选中状态，默认1选中
                    vo.setSelected(strToInt(selectMap.getOrDefault(skuKey, CartConstant.CART_SELECT)));
                    return vo;
                }).collect(Collectors.toList());

        // 5. 返回VO
        CartTotalVO totalVO = new CartTotalVO();
        totalVO.setCartList(cartVOList);
        CartCalTotalVO cartTotal = calculateCartTotal(cartVOList);
        totalVO.setTotalCount(cartTotal.getTotalCount());
        totalVO.setTotalAmount(cartTotal.getTotalAmount());
        return totalVO;
    }

    /**
     * 重载购物车Redis缓存：数据库购物车全量同步到Redis
     * 适用场景：Redis缓存过期丢失、下单（库缓存不一致时，写会缓存）
     */
    @Override
    public void reloadCartCache(List<Cart> cartList) {
        // 1. 登录校验
        User user = UserUtils.validUserLogin();
        Long userId = user.getId();

        // 2. 获取两个Redis Hash缓存
        RMap<String, Integer> cartRMap = getUserCartRMap();
        RMap<String, Integer> selectRMap = getUserCartSelectRMap(userId);

        // 3. 先清空原有缓存，避免脏数据残留
        cartRMap.clear();
        selectRMap.clear();

        // 数据库无购物车，清空缓存后直接返回
        if (CollUtil.isEmpty(cartList)) {
            cartRMap.expire(Duration.ofDays(CART_TTL_DAYS));
            selectRMap.expire(Duration.ofDays(CART_TTL_DAYS));
            log.info("用户{}数据库无购物车数据，已清空Redis缓存", userId);
            return;
        }

        // 4. 数据库数据组装批量Map，一次性写入Redis
        cartRMap.putAll(cartList.stream().collect(Collectors.toMap(
                        cart -> cart.getSkuId().toString(),
                        Cart::getCount
                )));
        selectRMap.putAll(cartList.stream().collect(Collectors.toMap(
                        cart -> cart.getSkuId().toString(),
                        cart -> cart.getSelected()
                )));

        // 统一设置30天过期时间
        cartRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        selectRMap.expire(Duration.ofDays(CART_TTL_DAYS));
        log.info("用户{}购物车缓存重载完成，共同步{}件商品", userId, cartList.size());
    }

    // 数据库新增、更新购物车记录
    private void saveOrUpdateCart(Long skuId,Integer count) {
        Long userId = SecurityUtils.getLoginUser().getUser().getId();
        // 校验该商品是否存在
        productSkuService.validSkuExist(skuId);
        // 校验购物车中是否存在
        Cart existCart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getSkuId, skuId));

        //其实无论新增、插入，都可直接设置总数量，因为都是从缓存获得的最新值（原子操作无脏数据），而不是变化值，所以无需再sql加减
        if (ObjectUtil.isNull(existCart)) {
            Cart cart = new Cart();
            // 无记录：新增
            cart.setUserId(userId);
            cart.setSkuId(skuId);
            cart.setCount(count);
            cart.setSelected(CartConstant.CART_SELECT);
            cartMapper.insert(cart);
        } else {
            // 已有记录：直接更新
            existCart.setCount(count);
            cartMapper.updateById(existCart);
        }
    }

    //计算购物车选中商品总数量、总金额
    private CartCalTotalVO calculateCartTotal(List<CartVO> cartVOList) {
        CartCalTotalVO result = new CartCalTotalVO();
        Integer totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 购物车为空
        if (CollUtil.isEmpty(cartVOList)) {
            result.setTotalCount(totalCount);
            result.setTotalAmount(totalAmount);
            return result;
        }

        // 购物车不为空
        for (CartVO vo : cartVOList) {
            // 只统计选中状态=1的商品
            if (Objects.equals(vo.getSelected(), CartConstant.CART_SELECT)) {
                totalCount += vo.getCount();
                totalAmount = totalAmount.add(vo.getSubTotal());
            }
        }
        result.setTotalCount(totalCount);
        result.setTotalAmount(totalAmount);
        return result;
    }

    // readAllMap()读出的对象是<Long,String> value根本不是自己设置Integer类型，因此要转换，避免报错
    // 读写分离优化：它只读取 Hash 全量数据，不抢占写线程，提升购物车列表查询并发吞吐量
    @Override
    public Integer strToInt(Object obj) {
        return Integer.parseInt(String.valueOf(obj));
    }

    // 校验单条购物车记录是否存在
    @Override
    public void validCartExist( Long skuId) {
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, SecurityUtils.getLoginUser().getUser().getId()).eq(Cart::getSkuId, skuId));
        if (cart == null) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
    }

    // 校验购物车缓存是否存在，不存在查库，有就回写缓存
    @Override
    public Map<String, Integer> validCartCacheExist(Map<String, Integer> allCartMap){
        if (CollUtil.isEmpty(allCartMap)) {
            // 缓存空，再查询数据库
            List<CartVO> cartVOList = getCartList().getCartList();
            // 数据库也没有购物车，直接抛异常
            if (CollUtil.isEmpty(cartVOList)) {
                throw new BusinessException(ResultCode.CART_EMPTY);
            }

            // 数据库存在购物车，重新加载到Redis缓存
            List<Cart> cartList =new ArrayList<>();
            cartVOList.stream().forEach(cartVO -> cartList.add(BeanUtil.copyProperties(cartVO, Cart.class)));
            reloadCartCache(cartList);
            // 重新回写，读取最新缓存
            allCartMap = getUserCartRMap().readAllMap();
        }
        return  allCartMap;
    }
}