package com.zh.hengyi.component.rabbitmq.seckill;

import com.rabbitmq.client.Channel;
import com.zh.hengyi.admin.mapper.seckill.SeckillLocalMsgConsumeMapper;
import com.zh.hengyi.admin.model.dto.seckill.SeckillOrderMsgDTO;
import com.zh.hengyi.admin.model.entity.seckill.SeckillLocalMsgConsume;
import com.zh.hengyi.admin.service.seckill.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.zh.hengyi.common.constant.SeckillConstant.SECKILL_LOCAL_MSG_CONSUME_STATUS_HAVING;
import static com.zh.hengyi.config.rabbitmq.SeckillRabbitConfig.SECKILL_ORDER_QUEUE;
import static org.springframework.amqp.rabbit.core.RabbitAdmin.QUEUE_NAME;

@Component
@Slf4j
public class SeckillOrderConsumer {

    private final SeckillOrderService seckillOrderService;
    private final SeckillLocalMsgConsumeMapper seckillLocalMsgConsumeMapper;

    public SeckillOrderConsumer(SeckillOrderService seckillOrderService, SeckillLocalMsgConsumeMapper seckillLocalMsgConsumeMapper) {
        this.seckillOrderService = seckillOrderService;
        this.seckillLocalMsgConsumeMapper = seckillLocalMsgConsumeMapper;
    }

    @RabbitListener(queues = SECKILL_ORDER_QUEUE)
    public void consume(SeckillOrderMsgDTO msgDTO, Message message, Channel channel) throws IOException {
        String msgId= msgDTO.getMsgId();
        try {

            // 构建消费记录
            SeckillLocalMsgConsume record = new SeckillLocalMsgConsume();
            record.setMsgId(msgId);
            record.setQueueName(SECKILL_ORDER_QUEUE);
            record.setStatus(SECKILL_LOCAL_MSG_CONSUME_STATUS_HAVING);

            // （一）“先查后改并发冲突覆盖”：如果先查询本地消息消费记录，再消费，可能会同时查到重复消费，所以直接修改，看结果
            // （二）“消息重复消费”：幂等校验，插入消费记录，唯一索引冲突，说明消息已处理
            // 1.1 幂等校验
            int insertCnt;
            try {
                insertCnt = seckillLocalMsgConsumeMapper.insert(record);
            } catch (DuplicateKeyException e) {
                log.info("秒杀消息重复消费，跳过 msgId:{}", msgId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //手动确认
                return;
            }

            // 1.2 兜底重试：如果出现未知异常插入失败（虽未重复，但可能违反库其他约束）
            if (insertCnt == 0) {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                return;
            }

            // 2. 通过，执行下单业务
            seckillOrderService.consumeSeckillOrder(msgDTO);

            // 3. 更新状态为消费成功
            seckillLocalMsgConsumeMapper.updateSuccess(msgDTO.getMsgId(), SECKILL_ORDER_QUEUE);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.error("秒杀消费异常 msg:{}",msgDTO,e);
            // 更新状态为消费失败
            seckillLocalMsgConsumeMapper.updateFail(msgDTO.getMsgId(), SECKILL_ORDER_QUEUE);
            // 异常拒绝，重回队列，后续配置死信队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }
    }
}
