package com.zh.hengyi.admin.controller.admin.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillGoodsAddDTO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/seckill")
@RequiredArgsConstructor
public class SeckillActivityController {
    private final SeckillActivityService seckillActivityService;

    // 1 查询秒杀活动分页
    @GetMapping("/page")
    public Result<IPage<SeckillActivityVO>> page(@Validated SeckillActivityQueryDTO dto){
        return Result.success(seckillActivityService.getPage(dto));
    }

    // 2 新增、编辑秒杀活动
    @PostMapping("/edit")
    public Result<Void> saveOrUpdate(@Valid @RequestBody SeckillActivityFormDTO dto) {
        seckillActivityService.saveOrUpdateActivity(dto);
        return Result.success();
    }

    // 3 删除秒杀活动
    @DeleteMapping("/{activityId}")
    public Result<Void> delete(@PathVariable Long activityId){
        seckillActivityService.deleteActivity(activityId);
        return Result.success();
    }

    // 4 开启秒杀活动
    @PostMapping("/open/{activityId}")
    public Result<Void> open(@PathVariable Long activityId){
        seckillActivityService.openSeckill(activityId);
        return Result.success();
    }

    // 5 关闭秒杀活动
    @PostMapping("/close/{activityId}")
    public Result<Void> close(@PathVariable Long activityId){
        seckillActivityService.closeSeckill(activityId);
        return Result.success();
    }

    // 6 获取秒杀活动详情，表单回显
    @GetMapping("/detail/{activityId}")
    public Result<SeckillActivityVO> getDetail(@PathVariable Long activityId) {
        return Result.success(seckillActivityService.getActivityDetail(activityId));
    }

}
