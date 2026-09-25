package com.zh.hengyi.application.mapper.seckill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.application.model.entity.BaseEntity;
import com.zh.hengyi.application.model.entity.seckill.SeckillLocalMsg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import static com.zh.hengyi.common.constant.SeckillConstant.*;

/**
* @author HENGGE
* @description 针对表【seckill_local_msg(秒杀本地消息表(本地消息表实现最终一致性))】的数据库操作Mapper
* @createDate 2026-09-11 23:46:46
* @Entity generator.domain.SeckillLocalMsg
*/
@Mapper
public interface SeckillLocalMsgMapper extends BaseMapper<SeckillLocalMsg> {

    default List<SeckillLocalMsg> selectWaitSendMsg(){
        return selectList(new LambdaQueryWrapper<SeckillLocalMsg>()
                .eq(SeckillLocalMsg::getStatus, SECKILL_LOCAL_MSG_STATUS_NO)
                .lt(SeckillLocalMsg::getRetryCount,SECKILL_LOCAL_MSG_RETRY_COUNT_MAX));
    };

    default void updateMsgSuccess(Long id){
        update(new LambdaUpdateWrapper<SeckillLocalMsg>()
                .eq(SeckillLocalMsg::getId,id)
                .set(SeckillLocalMsg::getStatus, SECKILL_LOCAL_MSG_STATUS_HAVING));
    };

    default void updateMsgFail(Long id){
        update(new LambdaUpdateWrapper<SeckillLocalMsg>()
                .eq(SeckillLocalMsg::getId,id)
                .set(BaseEntity::getStatus, SECKILL_LOCAL_MSG_STATUS_FAIL)
        );
    };

    default void increaseRetryCount(Long id){
        update(new LambdaUpdateWrapper<SeckillLocalMsg>()
                .eq(SeckillLocalMsg::getId,id)
                .setSql("retry_count = retry_count + 1"));
    };

    // 乐观锁获取消息
    default int tryLockMsg(SeckillLocalMsg msg){
        return update(new LambdaUpdateWrapper<SeckillLocalMsg>()
                .eq(SeckillLocalMsg::getId,msg.getId())
                .eq(SeckillLocalMsg::getVersion,msg.getVersion())
                .set(BaseEntity::getStatus, SECKILL_LOCAL_MSG_STATUS_DOING)
                .setSql("version = version + 1")
        );
    };
}




