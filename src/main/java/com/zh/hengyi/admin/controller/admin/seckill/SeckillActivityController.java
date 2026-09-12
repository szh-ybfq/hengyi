package com.zh.hengyi.admin.controller.admin.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillGoodsAddDTO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/seckill")
@RequiredArgsConstructor
@Tag(name = "秒杀活动管理模块")
public class SeckillActivityController {
    private final SeckillActivityService seckillActivityService;

    @GetMapping("/page")
    @Operation(summary = "查询秒杀活动分页")
    public Result<IPage<SeckillActivityVO>> page(@Validated SeckillActivityQueryDTO dto){
        return Result.success(seckillActivityService.getPage(dto));
    }

    @PostMapping("/edit")
    @Operation(summary = "新增、编辑秒杀活动")
    public Result<Void> saveOrUpdate(@Valid @RequestBody SeckillActivityFormDTO dto) {
        seckillActivityService.saveOrUpdateActivity(dto);
        return Result.success();
    }

    @DeleteMapping("/{activityId}")
    @Operation(summary = "删除秒杀活动")
    public Result<Void> delete(@PathVariable Long activityId){
        seckillActivityService.deleteActivity(activityId);
        return Result.success();
    }

    @PostMapping("/open/{activityId}")
    @Operation(summary = "开启秒杀活动")
    public Result<Void> open(@PathVariable Long activityId){
        seckillActivityService.openSeckill(activityId);
        return Result.success();
    }

    @PostMapping("/close/{activityId}")
    @Operation(summary = "关闭秒杀活动")
    public Result<Void> close(@PathVariable Long activityId){
        seckillActivityService.closeSeckill(activityId);
        return Result.success();
    }

    @GetMapping("/detail/{activityId}")
    @Operation(summary = "获取秒杀活动详情，表单回显")
    public Result<SeckillActivityVO> getDetail(@PathVariable Long activityId) {
        return Result.success(seckillActivityService.getActivityDetail(activityId));
    }

}
