package com.zh.hengyi.application.mapper.seckill;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zh.hengyi.application.model.entity.BaseEntity;
import com.zh.hengyi.application.model.entity.seckill.SeckillLocalMsgConsume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import static com.zh.hengyi.common.constant.SeckillConstant.SECKILL_LOCAL_MSG_CONSUME_STATUS_FAIL;
import static com.zh.hengyi.common.constant.SeckillConstant.SECKILL_LOCAL_MSG_CONSUME_STATUS_HAVING;

/**
* @author HENGGE
* @description 针对表【seckill_local_msg_consume】的数据库操作Mapper
* @createDate 2026-09-12 11:09:39
* @Entity com.zh.hengyi.admin.model.entity.seckill.SeckillLocalMsgConsume
*/
@Mapper
public interface SeckillLocalMsgConsumeMapper extends BaseMapper<SeckillLocalMsgConsume> {

    default void updateFail(String msgId, String seckillOrderQueue){
        update(new LambdaUpdateWrapper<SeckillLocalMsgConsume>()
                .eq(SeckillLocalMsgConsume::getMsgId,msgId)
                .eq(SeckillLocalMsgConsume::getQueueName,seckillOrderQueue)
                .set(BaseEntity::getStatus,SECKILL_LOCAL_MSG_CONSUME_STATUS_FAIL)
        );
    };

    default void updateSuccess(String msgId, String seckillOrderQueue){
        update(new LambdaUpdateWrapper<SeckillLocalMsgConsume>()
                .eq(SeckillLocalMsgConsume::getMsgId,msgId)
                .eq(SeckillLocalMsgConsume::getQueueName,seckillOrderQueue)
                .set(BaseEntity::getStatus,SECKILL_LOCAL_MSG_CONSUME_STATUS_HAVING)
        );
    };
}




