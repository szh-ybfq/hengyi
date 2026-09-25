package com.zh.hengyi.application.service.seckill;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zh.hengyi.application.model.entity.seckill.SeckillLocalMsg;

/**
* @author HENGGE
* @description 针对表【seckill_local_msg(秒杀本地消息表(本地消息表实现最终一致性))】的数据库操作Service
* @createDate 2026-09-11 23:46:46
*/
public interface SeckillLocalMsgService extends IService<SeckillLocalMsg> {

    void saveLocalMsg(SeckillLocalMsg seckillLocalMsg);
}
