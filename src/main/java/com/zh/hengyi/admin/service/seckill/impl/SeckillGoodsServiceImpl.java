package com.zh.hengyi.admin.service.seckill.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.seckill.SeckillGoodsMapper;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;
import com.zh.hengyi.admin.service.seckill.SeckillGoodsService;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import org.springframework.stereotype.Service;

/**
* @author HENGGE
* @description 针对表【seckill_goods(秒杀商品表)】的数据库操作Service实现
* @createDate 2026-08-28 07:28:08
*/
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {
    @Override
    public SeckillGoods validSeckillGoodsExist(Long seckillGoodsId){
        SeckillGoods seckillGoods = baseMapper.selectById(seckillGoodsId);
        if(seckillGoods==null){
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_EXIST);
        }
        return seckillGoods;
    };
}




