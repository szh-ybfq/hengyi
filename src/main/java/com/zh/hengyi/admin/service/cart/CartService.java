package com.zh.hengyi.admin.service.cart;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.cart.CartAddDTO;
import com.zh.hengyi.admin.model.dto.cart.CartSelectDTO;
import com.zh.hengyi.admin.model.dto.cart.CartUpdateCountDTO;
import com.zh.hengyi.admin.model.entity.cart.Cart;
import com.zh.hengyi.admin.model.vo.cart.CartTotalVO;
import org.redisson.api.RMap;

import java.util.List;
import java.util.Map;

/**
* @author HENGGE
* @description 针对表【cart(购物车表)】的数据库操作Service
* @createDate 2026-08-11 08:04:12
*/
public interface CartService extends IService<Cart> {

    RMap<String, Integer> getUserCartRMap();

    RMap<String, Integer> getUserCartSelectRMap();

    /**
     * 加入购物车
     */
    void addCart(CartAddDTO dto);

    /**
     * 修改购物车商品数量
     */
    void updateCount(CartUpdateCountDTO dto);

    // 删除购物车单个商品
    void removeCart(Long skuId);

    // 删除购物车已勾选商品
    void removeSelected(List<String> removeSkuKeys);

    // 修改选中状态 / 全选/全不选
    void updateSelect(CartSelectDTO dto);

    // 查询当前用户完整购物车（带汇总价格数量）
    CartTotalVO getCartList();

    // 登录校验
    Integer strToInt(Object obj);

    // 校验单条购物车记录是否存在
    Cart validCartExist( Long skuId);

    // 同步库中数据到缓存
    void reloadCartCache(List<Cart> dbCartList);

    // 校验购物车缓存是否存在，不存在查库，有就回写缓存
    Map<String, Integer> validCartCacheExist(Map<String, Integer> allCartMap,Map<String, Integer> allSelectStatus);

}
