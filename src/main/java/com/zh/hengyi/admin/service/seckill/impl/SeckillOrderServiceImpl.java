package com.zh.hengyi.admin.service.seckill.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.admin.mapper.order.OrderMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillActivityMapper;
import com.zh.hengyi.admin.mapper.seckill.SeckillGoodsMapper;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityFormDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillActivityQueryDTO;
import com.zh.hengyi.admin.model.dto.seckill.SeckillGoodsAddDTO;
import com.zh.hengyi.admin.model.entity.order.Order;
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
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements SeckillOrderService {

    @Override
    public Order validSeckillOrderExist(Long activityId) {
        return null;
    }
}


