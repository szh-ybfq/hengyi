package com.zh.hengyi.component.rabbitmq.productCache;

import lombok.Data;
import java.io.Serializable;

/**
 * 缓存延迟删除MQ消息载体
 */
@Data
public class CacheDelayMsgDTO implements Serializable {
    // 消息类型：singleKey 单key删除 / categoryPage 分类分页批量清理
    private String type;
    // 单个缓存key
    private String cacheKey;
    // 分类id
    private Long categoryId;
}