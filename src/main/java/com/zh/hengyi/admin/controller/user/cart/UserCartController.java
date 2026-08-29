package com.zh.hengyi.admin.controller.user.cart;

import com.zh.hengyi.admin.model.dto.cart.CartAddDTO;
import com.zh.hengyi.admin.model.dto.cart.CartSelectDTO;
import com.zh.hengyi.admin.model.dto.cart.CartUpdateCountDTO;
import com.zh.hengyi.admin.model.vo.cart.CartTotalVO;
import com.zh.hengyi.admin.service.cart.CartService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/cart")
@Tag(name = "用户购物车模块")
@RequiredArgsConstructor
public class UserCartController {
    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "加入购物车")
    public Result<Void> addCart(@Valid @RequestBody CartAddDTO dto) {
        cartService.addCart(dto);
        return Result.success();
    }

    @PutMapping("/update/count")
    @Operation(summary = "修改购物车商品数量")
    public Result<Void> updateCount(@Valid @RequestBody CartUpdateCountDTO dto) {
        cartService.updateCount(dto);
        return Result.success();
    }

    @DeleteMapping("/remove/{skuId}")
    @Operation(summary = "删除购物车商品")
    public Result<Void> removeCart(@PathVariable Long skuId) {
        cartService.removeCart(skuId);
        return Result.success();
    }

    @PutMapping("/update/select")
    @Operation(summary = "修改购物车商品选中状态")
    public Result<Void> updateSelect(@Valid @RequestBody CartSelectDTO dto) {
        cartService.updateSelect(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "查询当前用户购物车列表")
    public Result<CartTotalVO> getCartList() {
        CartTotalVO cartTotalVO = cartService.getCartList();
        return Result.success(cartTotalVO);
    }

}