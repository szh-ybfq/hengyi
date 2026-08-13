package com.zh.hengyi.admin.controller.user.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.order.OrderCreateDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.admin.model.dto.order.OrderQueryUserDTO;
import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.vo.order.OrderDetailVO;
import com.zh.hengyi.admin.model.vo.order.OrderPageVO;
import com.zh.hengyi.admin.service.order.OrderRefundService;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/order")
@Tag(name = "用户订单模块")
public class UserOrderController {

    @Autowired
    @Lazy
    private  OrderService orderService;

    @Autowired
    @Lazy
    private  OrderRefundService refundService;

    @PostMapping("/create")
    @Operation(summary = "购物车结算创建订单")
    public Result<Void> createOrder(@Valid @RequestBody OrderCreateDTO dto){
        orderService.createOrder(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "查询我的订单分页")
    public Result<IPage<OrderPageVO>> myOrderPage(OrderQueryUserDTO dto){
        return Result.success(orderService.getMyOrderPage(dto));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "订单详情")
    public Result<OrderDetailVO> getDetail(@PathVariable Long orderId){
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "取消未支付订单")
    public Result<Void> cancelOrder(@PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return Result.success();
    }

    @PostMapping("/refund/apply")
    @Operation(summary = "申请退款")
    public Result<Void> applyRefund(@Valid @RequestBody OrderRefundApplyDTO dto){
        refundService.applyRefund(dto);
        return Result.success();
    }
}