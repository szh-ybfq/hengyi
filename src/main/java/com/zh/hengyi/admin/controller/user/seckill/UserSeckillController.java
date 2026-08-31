package com.zh.hengyi.admin.controller.user.seckill;

import com.zh.hengyi.admin.model.dto.order.OrderRefundApplyDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderCreateDTO;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import com.zh.hengyi.common.result.Result;
import com.zh.hengyi.common.utils.security.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/seckill")
@RequiredArgsConstructor
public class UserSeckillController {

    private final SeckillOrderService seckillOrderService;

    // 创建秒杀下单请求
    @PostMapping("/create")
    @Operation(summary = "秒杀下单")
    public Result<Void> createSeckillOrder(@Valid @RequestBody SeckillOrderCreateDTO dto){
        // 校验登录
        Long userId = UserUtils.validUserLogin().getId();
        seckillOrderService.submitSeckillOrder(dto);
        return Result.success();
    }


    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "取消未支付订单")
    public Result<Void> cancelOrder(@PathVariable Long orderId){
        seckillOrderService.cancelSeckillOrder(orderId);
        return Result.success();
    }
//
//    @PostMapping("/refund/apply")
//    @Operation(summary = "申请退款")
//    public Result<Void> applyRefund(@Valid @RequestBody OrderRefundApplyDTO dto){
//        seckillOrderService.applySeckillRefund(dto);
//        return Result.success();
//    }
}

