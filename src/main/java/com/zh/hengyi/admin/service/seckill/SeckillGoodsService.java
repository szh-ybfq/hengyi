package com.zh.hengyi.admin.service.seckill;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.admin.model.entity.seckill.SeckillGoods;

/**
* @author HENGGE
* @description 针对表【seckill_goods(秒杀商品表)】的数据库操作Service
* @createDate 2026-08-28 07:28:08
*/
public interface SeckillGoodsService extends IService<SeckillGoods> {
    SeckillGoods validSeckillGoodsExist(Long seckillGoodsId);
}
