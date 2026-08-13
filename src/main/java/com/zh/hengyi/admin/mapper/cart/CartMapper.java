package com.zh.hengyi.admin.mapper.cart;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.cart.Cart;
import com.zh.hengyi.common.constant.CartConstant;
import com.zh.hengyi.config.sercurity.utils.SecurityUtils;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【cart(购物车表)】的数据库操作Mapper
* @createDate 2026-08-11 08:04:12
* @Entity generator.domain.Cart
*/
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    default void deleteSelected(){
        delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, SecurityUtils.getLoginUser().getUser().getId())
                .eq(Cart::getSelected, CartConstant.CART_SELECT)
                .eq(Cart::getStatus, CartConstant.CART_STATUS_NORMAL)
        );
    };
}




