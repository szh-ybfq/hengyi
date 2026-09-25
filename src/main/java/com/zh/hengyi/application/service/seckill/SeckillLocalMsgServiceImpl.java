package com.zh.hengyi.application.service.seckill;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.model.entity.seckill.SeckillLocalMsg;
import com.zh.hengyi.application.mapper.seckill.SeckillLocalMsgMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author HENGGE
* @description 针对表【seckill_local_msg(秒杀本地消息表(本地消息表实现最终一致性))】的数据库操作Service实现
* @createDate 2026-09-11 23:46:46
*/
@Service
public class SeckillLocalMsgServiceImpl extends ServiceImpl<SeckillLocalMsgMapper, SeckillLocalMsg> implements SeckillLocalMsgService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLocalMsg(SeckillLocalMsg seckillLocalMsg) {
        baseMapper.insert(seckillLocalMsg);
    }
}




