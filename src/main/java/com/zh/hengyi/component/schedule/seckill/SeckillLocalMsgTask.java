package com.zh.hengyi.component.schedule.seckill;


import com.alibaba.fastjson2.JSON;
import com.zh.hengyi.application.mapper.seckill.SeckillLocalMsgMapper;
import com.zh.hengyi.application.model.dto.seckill.SeckillOrderMsgDTO;
import com.zh.hengyi.application.model.entity.seckill.SeckillLocalMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zh.hengyi.common.constant.SeckillConstant.SECKILL_LOCAL_MSG_RETRY_COUNT_MAX;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillLocalMsgTask {

    private final SeckillLocalMsgMapper seckillLocalMsgMapper;

    private final RabbitTemplate rabbitTemplate;

    // 定时任务：每10s扫描一次本地消息表（作用：慢慢同步，最终达到最终一致性）
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void scanLocalMsg(){
        // 查询待发送的消息（status=0 待发送，（一）“避免死循环”：重试次数不能超过上限5次）
        List<SeckillLocalMsg> waitSendList = seckillLocalMsgMapper.selectWaitSendMsg();
        for (SeckillLocalMsg msg : waitSendList) {

            // （二）“避免多实例重复抢消息”：乐观锁获取消息，如果不一致则跳过
            int row = seckillLocalMsgMapper.tryLockMsg(msg);
            if (row == 0) { //版本号不同，不更新
                continue;
            }

            try {
                SeckillOrderMsgDTO dto = JSON.parseObject(msg.getMsgContent(), SeckillOrderMsgDTO.class);
                // 发送消息、更新消息状态
                rabbitTemplate.convertAndSend(msg.getExchange(),msg.getRoutingKey(), dto, new CorrelationData(msg.getMsgId()));
                seckillLocalMsgMapper.updateMsgSuccess(msg.getId());
            }catch (Exception e){
                log.error("秒杀消息发送失败 msgId:{}，当前重试次数:{}",msg.getMsgId(), msg.getRetryCount());

                // （三）“达到重试上限5次”：直接修改消息状态为失败，不再重查重试
                if (msg.getRetryCount()+1>SECKILL_LOCAL_MSG_RETRY_COUNT_MAX){
                    seckillLocalMsgMapper.updateMsgFail(msg.getId());
                    log.error("秒杀消息msgId:{} 达到最大重试次数{},标记失败，请人工排查",msg.getMsgId(),SECKILL_LOCAL_MSG_RETRY_COUNT_MAX);
                }else {
                    // 未达到上限，增加重试次数+1
                    seckillLocalMsgMapper.increaseRetryCount(msg.getId());
                }
            }
        }
    }
}

