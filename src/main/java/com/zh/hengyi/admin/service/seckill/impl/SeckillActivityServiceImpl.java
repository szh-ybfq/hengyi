package com.zh.hengyi.admin.service.seckill.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.seckill.SeckillActivityMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillGoodsMapper;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillGoodsAddDTO;
import com.zh.hengyi.admin.model.entity.product.ProductSku;
import com.zh.hengyi.admin.model.entity.product.ProductSpu;
import com.zh.hengyi.admin.model.entity.seckill.SeckillActivity;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.model.entity.stock.Stock;
import com.zh.hengyi.admin.model.vo.seckill.SeckillActivityVO;
import com.zh.hengyi.admin.model.vo.seckill.SeckillGoodsVO;
import com.zh.hengyi.admin.service.product.ProductSkuService;
import com.zh.hengyi.admin.service.product.ProductSpuService;
import com.zh.hengyi.admin.service.seckill.SeckillActivityService;
import com.zh.hengyi.admin.service.stock.StockService;
import com.zh.hengyi.common.constant.SeckillConstant;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.zh.hengyi.common.constant.SeckillConstant.GOODS_SOLD;

/**
* @author HENGGE
* @description 针对表seckill_activity(秒杀活动表)的数据库操作Service实现
* @createDate 2026-08-28 07:28:08
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    private final SeckillGoodsMapper seckillGoodsMapper;
    private final ProductSkuService skuService;
    private final ProductSpuService spuService;
    private final RedissonClient redissonClient;
    private final StockService stockService;

    // 1 查询秒杀活动分页
    @Override
    public IPage<SeckillActivityVO> getPage(SeckillActivityQueryDTO dto) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<SeckillActivity>()
                .like(StrUtil.isNotBlank(dto.getActivityName()), SeckillActivity::getActivityName, dto.getActivityName())
                .eq(dto.getStatus() != null, SeckillActivity::getStatus, dto.getStatus())
                .ge(dto.getStartTime() != null, SeckillActivity::getEndTime, dto.getStartTime())
                .le(dto.getEndTime() != null, SeckillActivity::getStartTime, dto.getEndTime())
                .orderByDesc(SeckillActivity::getCreateTime);

        IPage<SeckillActivity> page = this.page(new Page<>(
                        dto.getPageNum() == null ? 1L : dto.getPageNum(),
                        dto.getPageSize() == null ? 10L : dto.getPageSize()),
                wrapper);

        return page.convert(item -> BeanUtil.copyProperties(item, SeckillActivityVO.class));
    }

    // 2 新增/编辑秒杀活动：同时保存 秒杀活动 + 商品列表
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateActivity(SeckillActivityFormDTO dto) {
        SeckillActivity activity;
        if (dto.getId() == null) {
            // 1 新增秒杀活动
            // 秒杀活动是否重名
            validSeckillActivityNameUnique(dto.getActivityName());
            activity = new SeckillActivity();
            BeanUtil.copyProperties(dto, activity);
            activity.setStatus(SeckillConstant.STATUS_NOT_START);
            this.save(activity);
            log.info("新增秒杀活动 id:{}成功", activity.getId());

            // 2 新增秒杀商品：从普通可用库存扣减秒杀库存
            addSeckillGoods(dto.getGoodsList(),activity.getId());
        } else {
            // 1 编辑秒杀活动
            activity = validActivitExist(dto.getId());

            // 校验秒杀活动是否开始（仅未开始的可以编辑）
            validSeckillActivityNotStart(activity);

            // 校验秒杀活动是否正在进行中
            validSeckillActivityNotRunning(activity);

            // 2 取出旧秒杀商品，先归还未售出的库存回主SKU
            List<SeckillGoods> oldGoodsList = getGoodsListByActivityId(activity.getId());
            for (SeckillGoods oldGoods : oldGoodsList) {
                // 归还 = 配置秒杀总库存 − 已经卖掉的数量
                int revertNum = oldGoods.getSeckillStock() - oldGoods.getSeckillSold();
                if(revertNum > 0){
                    // 校验库存是否存在
                    Stock stock = stockService.validStockExist(oldGoods.getSkuId());
                    // 归还回sku普通可用库存
                    stockService.revertAvailableStock(stock, revertNum);
                }
            }

            //逻辑删除旧的秒杀商品记录
            seckillGoodsMapper.update(null,new LambdaUpdateWrapper<SeckillGoods>().eq(SeckillGoods::getActivityId, activity.getId()).set(SeckillGoods::getDeleted, 1));
            log.info("删除秒杀活动 id:{}下的所有秒杀商品，已归还未售出库存", activity.getId());

            //更新秒杀活动主表
            BeanUtil.copyProperties(dto, activity);
            this.updateById(activity);
            log.info("修改秒杀活动成功");

            // 3 新增秒杀商品：从普通可用库存扣减秒杀库存
            addSeckillGoods(dto.getGoodsList(),activity.getId());
        }
    }

    // 新增秒杀商品
    private void addSeckillGoods(List<SeckillGoodsAddDTO> seckillGoodsList,Long activityId){
        for (SeckillGoodsAddDTO goods : seckillGoodsList) {
            Long skuId = goods.getSkuId();
            Integer seckillStock = goods.getSeckillStock();

            // 1.校验sku库存是否存在
            Stock stock = stockService.validStockExist(skuId);

            // 2.校验sku可用库存是否充足
            stockService.validStockAvailable(stock,seckillStock);

            // 3.扣减普通可用库存，把这部分划拨给秒杀使用（乐观锁扣库存）
            stockService.deductAvailableStock(stock, seckillStock);

            // 4.保存seckillGoods，写入秒杀总库存、已售初始=0
            SeckillGoods seckillGoods = new SeckillGoods();
            seckillGoods.setActivityId(activityId);
            seckillGoods.setSkuId(skuId);
            seckillGoods.setSeckillPrice(goods.getSeckillPrice());
            seckillGoods.setSeckillStock(seckillStock); // 配置秒杀总库存
            seckillGoods.setSeckillSold(SeckillConstant.GOODS_SOLD); // 已售初始0
            seckillGoods.setLimitPerson(goods.getLimitPerson()); // 每日限售
            seckillGoodsMapper.insert(seckillGoods);
        }
    }

    // 3 删除秒杀活动以及秒杀商品
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId) {
        // 校验秒杀活动存在
        SeckillActivity activity = validActivitExist(activityId);
        // 校验秒杀活动是否正在进行中
        validSeckillActivityNotRunning(activity);
        // 校验秒杀活动是否有秒杀商品
        validSeckillActivityExistGoods(activityId);

        this.removeById(activityId);
        seckillGoodsMapper.update(null, new LambdaUpdateWrapper<SeckillGoods>().eq(SeckillGoods::getActivityId, activity.getId()).set(SeckillGoods::getDeleted, 1));
        log.info("删除秒杀活动 id:{}，以及秒杀商品", activityId);
    }

    // 4 开启秒杀活动
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openSeckill(Long activityId) {
        // 校验：秒杀活动存在
        SeckillActivity activity = validActivitExist(activityId);

        // 校验：秒杀活动必须是未开始
        validSeckillActivityNotStart(activity);

        // 校验：秒杀活动必须含有秒杀商品
        List<SeckillGoods> goodsList = getGoodsListByActivityId(activityId);
        if (CollUtil.isEmpty(goodsList)) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_EMPTY);
        }

        //Redis预热，缓存秒杀商品库存、没人限购
        for (SeckillGoods goods : goodsList) {
            //todo:缓存时间应该等于 活动开始起止时间
            redissonClient.getBucket(SeckillConstant.SECKILL_STOCK_PREFIX + goods.getId()).set(goods.getSeckillStock());
            redissonClient.getBucket(SeckillConstant.SECKILL_USER_LIMIT_PREFIX + goods.getId()).set(goods.getLimitPerson());
        }
        log.info("秒杀活动{}：Redis预热完成", activityId);

        activity.setStatus(SeckillConstant.STATUS_RUNNING);
        this.updateById(activity);
    }

    // 5 关闭秒杀活动
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSeckill(Long activityId) {
        // 校验：秒杀活动存在
        SeckillActivity activity = validActivitExist(activityId);

        // 校验：秒杀活动必须是进行中
        if (!SeckillConstant.STATUS_RUNNING.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.SECKILL_CLOSE_FORBID, "仅进行中活动支持关闭");
        }

        // 删除Redis秒杀商品缓存
        for (SeckillGoods goods : getGoodsListByActivityId(activityId)) {
            redissonClient.getBucket(SeckillConstant.SECKILL_STOCK_PREFIX + goods.getId()).delete();
            redissonClient.getBucket(SeckillConstant.SECKILL_USER_LIMIT_PREFIX + goods.getId()).delete();
        }
        activity.setStatus(SeckillConstant.STATUS_FINISH);

        this.updateById(activity);
        log.info("秒杀活动{}手动关闭，清理Redis缓存完成", activityId);
    }

    // 6 获取秒杀活动详情，编辑表单回显，秒杀活动信息 + 全部秒杀商品
    @Override
    public SeckillActivityVO getActivityDetail(Long activityId) {
        // 校验秒杀活动存在
        SeckillActivity activity = validActivitExist(activityId);
        // 查询关联秒杀商品，填充VO用于表单回显
        SeckillActivityVO vo = BeanUtil.copyProperties(activity, SeckillActivityVO.class);
        vo.setGoodsList(getGoodsListByActivityId(activityId)
                .stream()
                .map(goods -> {
                    SeckillGoodsVO item = BeanUtil.copyProperties(goods, SeckillGoodsVO.class);
                    ProductSku sku = skuService.getById(goods.getSkuId());
                    ProductSpu spu = spuService.getById(sku.getSpuId());
                    item.setSkuSpec(sku.getSkuSpec());
                    item.setSpuName(spu.getSpuName());
                    return item;
                }).collect(Collectors.toList()));
        return vo;
    }

    // 6_1 获取秒杀活动下秒杀商品列表
    @Override
    public List<SeckillGoods> getGoodsListByActivityId(Long activityId) {
        return seckillGoodsMapper.selectList(new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getActivityId, activityId));
    }





    // 校验：秒杀活动存在
    @Override
    public SeckillActivity validActivitExist(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.SECKILL_ACTIVITY_NOT_EXIST);
        }
        return activity;
    }
    // 校验：活动不能处于进行中，用于编辑、删除、新增商品
    @Override
    public void validSeckillActivityNotRunning(SeckillActivity activity) {
        if (SeckillConstant.STATUS_RUNNING.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.SECKILL_ACTIVITY_RUNNING_FORBID);
        }
    }
    // 校验：活动必须是未开始，仅未开始允许编辑活动、开启活动
    @Override
    public void validSeckillActivityNotStart(SeckillActivity activity) {
        if (!SeckillConstant.STATUS_NOT_START.equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.SECKILL_ACTIVITY_EDIT_FORBID);
        }
    }
    // 校验：秒杀活动是否有秒杀商品
    @Override
    public void validSeckillActivityExistGoods(Long activityId) {
        SeckillGoods goods = seckillGoodsMapper.selectOne(new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getActivityId, activityId));
        if (goods!=null) {
            throw new BusinessException(ResultCode.SECKILL_ACTIVITY_EXIST_GOODS);
        }
    }
    // 校验：秒杀活动是否重名
    @Override
    public void validSeckillActivityNameUnique(String name) {
        SeckillActivity goods = baseMapper.selectOne(new LambdaQueryWrapper<SeckillActivity>().eq(SeckillActivity::getActivityName, name));
        if (goods!=null) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NAME_NOT_UNIQUE);
        }
    }
}


