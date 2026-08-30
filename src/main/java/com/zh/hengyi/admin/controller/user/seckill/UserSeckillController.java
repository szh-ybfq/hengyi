package com.zh.hengyi.admin.controller.user.seckill;

import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderCreateDTO;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import com.zh.hengyi.common.result.Result;
import com.zh.hengyi.common.utils.security.UserUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/seckill")
@RequiredArgsConstructor
public class UserSeckillController {

    private final SeckillOrderService seckillOrderService;

    /**
     * 创建秒杀下单请求
     * Sentinel 在这个接口配置：热点参数限流、QPS限流
     */
    @PostMapping("/create")
    public Result<Void> createSeckillOrder(@Valid @RequestBody SeckillOrderCreateDTO dto){
        // 校验登录
        Long userId = UserUtils.validUserLogin().getId();
        seckillOrderService.submitSeckillOrder(dto);
        return Result.success();
    }
}

