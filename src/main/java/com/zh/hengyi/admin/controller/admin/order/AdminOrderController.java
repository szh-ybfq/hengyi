package com.zh.hengyi.admin.controller.admin.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.order.OrderQueryAdminDTO;
import com.zh.hengyi.admin.model.vo.order.OrderDetailVO;
import com.zh.hengyi.admin.model.vo.order.OrderPageVO;
import com.zh.hengyi.admin.service.order.OrderService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/v1/order")
@Tag(name = "后台订单管理模块")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping("/page")
    @Operation(summary = "后台全部订单分页查询")
    public Result<IPage<OrderPageVO>> adminOrderPage(OrderQueryAdminDTO dto){
        return Result.success(orderService.getAdminOrderPage(dto));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "后台查看订单详情")
    public Result<OrderDetailVO> getDetail(@PathVariable Long orderId){
        return Result.success(orderService.getOrderDetail(orderId));
    }

    // TODO 迭代补充：发货接口、退款审核接口
}