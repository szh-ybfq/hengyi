package com.zh.hengyi.admin.service.seckill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillGoodsAddDTO;
import com.zh.hengyi.admin.model.entity.seckill.SeckillActivity;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import java.util.List;
/**
* @author HENGGE
* @description 针对表【seckill_activity(秒杀活动表)】的数据库操作Service
* @createDate 2026-08-28 07:28:08
*/



public interface SeckillActivityService extends IService<SeckillActivity> {
    // 分页查询秒杀活动
    IPage<SeckillActivityVO> getPage(SeckillActivityQueryDTO dto);

    // 新增、编辑秒杀活动
    void saveOrUpdateActivity(SeckillActivityFormDTO dto);

    // 删除秒杀活动以及商品
    void deleteActivity(Long activityId);

    // 开启秒杀
    void openSeckill(Long activityId);

    // 关闭秒杀
    void closeSeckill(Long activityId);

    // 获取秒杀活动详情，表单回显，秒杀活动信息 + 全部秒杀商品
    SeckillActivityVO getActivityDetail(Long activityId);

    // 根据秒杀活动获取秒杀商品列表
    List<SeckillGoods> getGoodsListByActivityId(Long activityId);

    SeckillActivity validActivitExist(Long activityId);
    void validSeckillActivityNotRunning(SeckillActivity activity);
    void validSeckillActivityNotStart(SeckillActivity activity);
    void validSeckillActivityExistGoods(Long activityId);
    void validSeckillActivityNameUnique(String name);
}

