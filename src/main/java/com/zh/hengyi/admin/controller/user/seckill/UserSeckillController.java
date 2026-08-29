package com.zh.hengyi.admin.controller.user.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.order.SeckillOrderCreateDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import com.zh.hengyi.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/seckill")
@RequiredArgsConstructor
public class UserSeckillController {
    private final SeckillOrderService seckillOrderService;

    // 1 创建秒杀单
    @PostMapping("/create")
    public Result<Void> page(@Valid @RequestBody SeckillOrderCreateDTO dto){

        return Result.success();
    }

}
