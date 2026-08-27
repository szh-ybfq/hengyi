package com.zh.hengyi.common.utils.cache.product;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zh.hengyi.admin.mapper.product.ProductCategoryMapper;
import com.zh.hengyi.admin.model.dto.product.ProductSpuQueryDTO;
import com.zh.hengyi.admin.model.entity.product.ProductCategory;
import com.zh.hengyi.common.exception.BusinessException;
import com.zh.hengyi.common.result.ResultCode;
import com.zh.hengyi.component.rabbitmq.productCache.CacheDelayMsgDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.zh.hengyi.config.rabbitmq.CacheDelayedMqConfig.CACHE_DELAY_EXCHANGE;
import static com.zh.hengyi.config.rabbitmq.CacheDelayedMqConfig.CACHE_DELAY_ROUTE_KEY;

@Component
@Slf4j
public class ProductCacheUtils {
    // 1. Caffeine：JVM进程本地缓存，读写不通过网络，毫秒级
    @Resource(name = "productLocalCacheManager")
    private CacheManager caffeineCacheManager;

    @Resource
    private ProductCategoryMapper productCategoryMapper;



    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;


    //统一管理常量名
    // Caffine 本地缓存分组名
    public static final String CACHE_NAME = "product_page";

    // redis 商品缓存key前缀
    public static final String CACHE_KEY_PREFIX = "product:page:";
    // redission 分布式锁前缀
    public static final String LOCK_KEY_PREFIX = "lock:product:";
    // rabbitmq 消息名
    public static final String MQ_MESSAGE_NAME = "cacheKey";

    // Redis基础过期时间 30分钟
    private static final long BASE_TTL = 30 * 60L;
    private static final long CACHE_NULL_TTL = 5 * 60L;
    // 分布式锁等待时间 200ms、持有时间 1500ms
    private static final long LOCK_WAIT_TIME = 200L;
    private static final long LOCK_HOLD_TIME = 1500L;
    // 缓存延迟删除时间 500ms
    private static final long DELAY_DELETE_TIME = 500L;
    // 随机过期时间
    private static final Random RandomTtl = new Random();
    // 时间单位统一毫秒
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    // 为何使用布隆过滤器？
        // 因为缓存空值会向redis疯狂大量写入null值，redis内存占满，而且只能抵挡5m,同时无法提前直接拦截不存在的业务id
        //布隆过滤器从源头避免数据库根本没有的数据，防止缓存穿透
    // 布隆过滤器（hash结构）：键：布隆过滤器的名字，值：商品缓存key
    private static final String BLOOM_PRODUCT_KEY = "bloom:product:category";
    // 预估元素数量、误判率（判定这个key存在，但是实际上并不存在）（可按需调整）
    private static final long BLOOM_EXPECT_NUM = 100000;
    private static final double BLOOM_FALSE_RATE = 0.01;
    // 缓存预热开关（布隆缓存全部预热完成，才会开启）
    public volatile boolean bloomReady = false;    //volatile：保证可见性（一个线程修改变量，其他线程立刻读到最新值）

    // 启动线程执行时，预热布隆过滤器
    /* @PostConstruct
    public void initBloom() {
        RBloomFilter<Long> bloom = getProductBloom();
        new Thread(() -> {
            try {
                // 已初始化会直接跳过。预估10万元素，误判率1%（已存在则不会重复初始化）
                bloom.tryInit(BLOOM_EXPECT_NUM, BLOOM_FALSE_RATE);
                // 全量查询数据库所有分类id，批量加到布隆
                List<Long> categoryIds = productCategoryMapper.selectList(null).stream()
                        .map(ProductCategory::getId)
                        .collect(Collectors.toList());
                categoryIds.forEach(bloom::add);
                bloomReady = true;
                log.info("商品分类布隆预热完成，加载分类数量：{}", categoryIds.size());
            } catch (Exception e) {
                log.error("分类布隆过滤器预热失败", e);
                bloomReady = false;
            }
        }).start();
    }
*/
    // 初始化布隆过滤器
    @Async
    public void initBloom() {
        RBloomFilter<Long> bloom = getProductBloom();
        try {
            // 已初始化会直接跳过。预估10万元素，误判率1%（已存在则不会重复初始化）
            bloom.tryInit(BLOOM_EXPECT_NUM, BLOOM_FALSE_RATE);
            // 全量查询数据库所有分类id，批量加到布隆
            List<Long> categoryIds = productCategoryMapper.selectList(null).stream()
                    .map(ProductCategory::getId)
                    .collect(Collectors.toList());
            categoryIds.forEach(bloom::add);
            bloomReady = true;
            log.info("商品分类布隆预热完成，加载分类数量：{}", categoryIds.size());
        } catch (Exception e) {
            log.error("分类布隆过滤器预热失败", e);
            bloomReady = false;
        }
    }

    // 获取布隆过滤器（只存商品类别id Long类型）
    public RBloomFilter<Long> getProductBloom() {
        return redissonClient.getBloomFilter(BLOOM_PRODUCT_KEY);
    }

    /**
     * 二级缓存查询：先本地Caffeine → Redis → DB回填
     * @param cacheKey 缓存key
     * @param dbQueryFunc 数据库查询逻辑（函数式）
     * @return 分页商品数据
     */
    public <T> T getTwoLevelCache(String cacheKey,  Supplier<T> dbQueryFunc) {
        // 1. 查询本地缓存Caffeine
        Cache<Object, Object> caffeineCache = (Cache<Object, Object>) caffeineCacheManager.getCache(CACHE_NAME).getNativeCache();
        Object localVal = caffeineCache.getIfPresent(cacheKey);//getIfPresent：不存在返回 null，不会自动加载
        if (localVal != null) {
            // 判断是空缓存标记
            if (isEmptyMarker(localVal)) {
                log.info("本地缓存Caffeine命中空值标记，数据库根本不存在该数据，直接返回");
                return null;
            }
            log.info("查询一级缓存Caffeine成功");
            return (T) localVal;
        }

        // 2. 查询Redis分布式缓存
        Object redisVal = redissonClient.getBucket(cacheKey).get();
        if (redisVal != null) {
            if (isEmptyMarker(redisVal)) {
                log.info("redis缓存命中空值标记，数据库根本不存在该数据，返回并写入本地缓存Caffeine空值标记");
                caffeineCache.put(cacheKey, EmptyCacheMarker.INSTANCE);
                return null;
            }
            // redis缓存不为空null，再转为String，防止报错
            String redisStr = redisVal.toString();
            // 2.1 Redis有数据，直接返回，并回填本地缓存caffeine，下次不用再查redis
            T pageData = (T) jsonToObj(redisStr, Page.class);// 反序列化为目标分页对象，返回值是 Page 固定类型，但方法泛型是 T，编译器无法判定 Page 一定匹配 T，因此报类型不匹配错误。
            caffeineCache.put(cacheKey, pageData);
            log.info("查询二级缓存redis成功,并放入本地缓存Caffeine成功");
            return pageData;
        }

        // 2.2 💎 Redis无数据，加分布式锁 防止缓存击穿（是单个缓存过期（一个，热点商品），但是几百个请求刚并发 同时打到数据库），让仅1个请求查DB
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX+cacheKey);
        boolean getLock = false;
//        int retryTimes = 5; // 最大重试次数
//        long sleepMs = 100L; // 每次重试休眠毫秒
        try {
            /*
            // tryLock抢锁，未抢到内置3s等待，再休眠100ms，然后再自旋重试抢锁
            for (int i = 0; i < retryTimes; i++) {
                getLock = lock.tryLock(LOCK_WAIT_TIME, LOCK_HOLD_TIME, TimeUnit.SECONDS);
                if (getLock) {
                    break;
                }
                Thread.sleep(sleepMs);
            }

            // 多次重试仍没拿到锁，才抛出异常，直接失败返回
            if (!getLock) {
                log.warn("获取商品缓存锁超时，重试{}次仍失败 key:{}", retryTimes, cacheKey);
                throw new BusinessException(ResultCode.CACHE_LOCK_TIMEOUT);
            }

            // 优化：仅尝试一次，最多阻塞3s，拿不到直接返回false
            getLock = lock.tryLock(LOCK_WAIT_TIME, LOCK_HOLD_TIME, TIME_UNIT);
            if (!getLock) {
                log.warn("获取商品缓存锁超时，key:{}", cacheKey);
                throw new BusinessException(ResultCode.CACHE_LOCK_TIMEOUT);
            }*/

            // 双重校验：加锁后再次查Redis，防止其他线程已回填缓存（可能其他线程又已经写入了），并回填本地缓存
            Object doubleCheck = redissonClient.getBucket(cacheKey).get();
            if (doubleCheck != null) {
                if (isEmptyMarker(doubleCheck)) {
                    log.info("双重校验，加锁后再次查Redis获取缓存发现空值标记，数据库根本不存在该数据，返回并写入本地缓存Caffeine空值标记");
                    caffeineCache.put(cacheKey, EmptyCacheMarker.INSTANCE);
                    return null;
                }
                String doubleCheckString = doubleCheck.toString();
                T pageData = (T) jsonToObj(doubleCheckString, Page.class);
                caffeineCache.put(cacheKey, pageData);
                log.info("双重校验，加锁后再次查Redis获取缓存数据成功，并放入本地缓存Caffeine成功");
                return (T) pageData;
            }

            // 3. 执行数据库查询
            T dbData = dbQueryFunc.get();

            // 4. 💎 数据根本不存在，缓存空值、布隆过滤器 处理缓存穿透（查询不存在商品，大量空请求击打DB），5分钟过期
            boolean emptyData;
            // 区分是集合还是普通对象，再判断是否为空数据
            if (dbData instanceof Collection) {
                emptyData = CollectionUtils.isEmpty((Collection<?>) dbData);
            } else {
                emptyData = (dbData == null);
            }
            // 数据不存在，返回空，缓存空值，并回填本地缓存
            if (emptyData) {
                String emptyJson = objToJson(EmptyCacheMarker.INSTANCE);
//                redissonClient.getBucket(cacheKey).set(emptyJson ,CACHE_NULL_TTL, TimeUnit.MINUTES);
                long nullRandomTtl = CACHE_NULL_TTL + RandomTtl.nextLong(60 * 60L);
                redissonClient.getBucket(cacheKey).set(emptyJson ,nullRandomTtl, TimeUnit.SECONDS);
                caffeineCache.put(cacheKey, EmptyCacheMarker.INSTANCE);
                log.info("数据不存在，返回空并写入一二级缓存空值标记");
                return null;
            }

            /*// 数据真实存在，添加到布隆过滤器
                // 绝对不能删除，兜底保障，因为后台新增商品、新增分类，产生全新 cacheKey，不及时添加，永远会被拦截，不会查库，
                //️ ❗️：必须在商品分类-新增分类时，及时填加到布隆过滤器，否则永远没有，永远会被拦截
            String[] keyArr = cacheKey.split(":");
            Long categoryId = null;
            if (keyArr.length >= 6) {
                String catStr = keyArr[5];
                // 排除"null"空字符串、空文本再转换
                if (StrUtil.isNotBlank(catStr) && !"null".equals(catStr)) {
                    bloom.add(CACHE_KEY_PREFIX + categoryId);
                }
            }*/

            // 6. 💎 正常数据写入一二级缓存，随机过期时间解决缓存雪崩（大量缓存（上万条） 同一时间全过期，基础过期30min，追加0~60min随机值）
            long randomTtl = BASE_TTL + RandomTtl.nextLong(60 * 60L);
            String pageJson = objToJson(dbData);
            redissonClient.getBucket(cacheKey).set(pageJson, randomTtl, TimeUnit.SECONDS);

            caffeineCache.put(cacheKey, dbData);

            log.info("查询数据库成功，写入一二级缓存成功");
            return dbData;

        } catch (BusinessException lockException) {
            throw lockException;// 锁专属异常直接上抛
        } catch (Exception e) {
            throw new BusinessException(ResultCode.CACHE_QUERY_EMPTY);// 其他IO、查询异常才包装为空缓存异常
        } finally {
            if (getLock && lock.isHeldByCurrentThread()) {
                lock.unlock(); //解锁
            }
        }
    }

    /**
     * 1、清理单个缓存值
     */
    public void clearSingleCache(String cacheKey) {
        // 1. 立即删除本地缓存
        caffeineCacheManager.getCache(CACHE_NAME).evict(cacheKey);
        // 2. 立即删除Redis缓存
        redissonClient.getBucket(cacheKey).delete();
        // 3. 发送延迟删除消息给rabmq队列，500ms后二次删除缓存，解决读写并发脏数据
        sendDelayDeleteMsg(cacheKey, DELAY_DELETE_TIME);
        log.info("清除单条分页key缓存成功");
    }

    /**
     * 2、清除商品该分类下所有分页缓存   （商品新增、删除、修改（分类、商品名、上下架、售价）时用）
     */
    public void clearCategoryPageCache(Long categoryId) {
        // 1、Caffeine本地缓存
        CaffeineCache cache = (CaffeineCache) caffeineCacheManager.getCache(CACHE_NAME);
        cache.invalidate();
        // 强制同步清理所有待删除条目，主线程阻塞至清理完成,完成后再执行后面线程
        cache.getNativeCache().cleanUp();

        // 2、Redis：删除商品该分类下所有分页缓存，
        // 💎 最重要坑：原来的不带分类id查询（例如只分页查、点数字、名称查）还残留脏数据，因此必须清除
        String pagePattern = CACHE_KEY_PREFIX + "*:*:*:" + categoryId + ":*";
        String pagePatternIndex = CACHE_KEY_PREFIX + "*:*:*:" + 0 + ":*";
        batchDelRedisByPattern(pagePattern);
        batchDelRedisByPattern(pagePatternIndex);

        // 3、发送500ms延迟批量清理消息兜底
        sendDelayDeleteMsg("category_page", null, categoryId, DELAY_DELETE_TIME);
        log.info("清除商品该分类下所有分页缓存成功");
    }

    /**
     * 3、清理该分类下所有类型缓存    （分类新增、启禁用时使用，分类修改不需要使用）
     *  TODO：将来做商品详情、首页搜索等等，缓存用到分类去设计的，全部要删除
     */
    public void clearCategoryAllCache(Long categoryId) {
        // TODO：❌️ 这里之后根据缓存键设计情况，清理该分类下所有类型缓存, 这里先仅仅删除商品分页缓存，所以不启用
        // 1、清空本地全部商品缓存
        caffeineCacheManager.getCache(CACHE_NAME).invalidate();
        // 2、Redis匹配该分类所有key删除
        // String CACHE_KEY_PREFIX1 = CACHE_KEY_PREFIX + "*:*:*:" + categoryId + ":*";
        // String CACHE_KEY_PREFIX2 = CACHE_KEY_PREFIX + "*:*:*:" + categoryId + ":*";
        // String CACHE_KEY_PREFIX3 = CACHE_KEY_PREFIX + "*:*:*:" + categoryId + ":*";
        String allPattern = CACHE_KEY_PREFIX + "*:*:*:" + categoryId + ":*";
        String pagePatternIndex = CACHE_KEY_PREFIX + "*:*:*:" + 0 + ":*";
        batchDelRedisByPattern(allPattern);
        batchDelRedisByPattern(pagePatternIndex);

        // 3、延迟二次全分类清理
        sendDelayDeleteMsg("category_all_type", null, categoryId, DELAY_DELETE_TIME);
    }

    /**
     * 私有工具：根据正则pattern批量删除redis key
     */
    private void batchDelRedisByPattern(String pattern) {
        // 获取迭代器
        Iterable<String> keyIterable = redissonClient.getKeys().getKeysByPattern(pattern);
        List<String> list = new ArrayList<>();
        // 转成集合
        for (String item : keyIterable) {
            list.add(item);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            String[] keyArr = list.toArray(new String[0]);  // List转String数组，直接传入delete
            redissonClient.getKeys().delete(keyArr);
        }
    }

    // 原有单key发送方法兼容改造，调用重载方法
    private void sendDelayDeleteMsg(String cacheKey, long delayMs) {
        sendDelayDeleteMsg("single_key", cacheKey, null, delayMs);
    }

    /**
     * 发送 延迟删除消息 💎
     *      应用场景：高并发下，A修改，B在A修改事务间隙请求，因为快照读，读到脏数据，又写回了缓存，因此要再删除
     *      私有重载：统一发送延迟删除消息，兼容单key/分类分页/全分类
     * @param type 消息类型 single_key / category_page / category_all
     * @param cacheKey 单key删除时传，批量删除传null
     * @param categoryId 分类批量删除传，单key传null
     * @param delayMs 延迟毫秒
     */
    private void sendDelayDeleteMsg(String type, String cacheKey, Long categoryId, Long delayMs) {
        // 替换HashMap，使用DTO封装数据
        CacheDelayMsgDTO dto = new CacheDelayMsgDTO();
        dto.setType(type);
        dto.setCacheKey(cacheKey);
        dto.setCategoryId(categoryId);
        rabbitTemplate.convertAndSend(CACHE_DELAY_EXCHANGE, CACHE_DELAY_ROUTE_KEY, dto, message -> {
            // 设置延迟时间头
            message.getMessageProperties().setHeader("x-delay", delayMs);
            return message;
        });
    }


    // 创建缓存key
    public String buildCacheKey(ProductSpuQueryDTO dto) {
        // 1. 数字参数：为空才转换，null转为"0"
        String pageNum = Objects.toString(dto.getPageNum(), "1");
        String pageSize = Objects.toString(dto.getPageSize(), "10");
        String categoryId = Objects.toString(dto.getCategoryId(), "0");
        String status = Objects.toString(dto.getStatus(), "1");

        // 2. 关键词搜索参数：null转空字符串 + URL编码，屏蔽冒号、空格等特殊符号
        String rawName = Objects.toString(dto.getSpuName(), "");
        // 最多保留50个字符，超出截断
        if(rawName.length() > 50){
            rawName = rawName.substring(0,50);
        }
        String safeSpuName = URLEncoder.encode(rawName, StandardCharsets.UTF_8);

        return CACHE_KEY_PREFIX + pageNum + ":"
                + pageSize + ":"
                + safeSpuName + ":"
                + categoryId + ":"
                + status;
    }

    // 对象转JSON字符串
    private String objToJson(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    // JSON字符串转回目标泛型对象
    private <T> T jsonToObj(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSONUtil.toBean(json, clazz);
    }

    // 空值对象
    public static class EmptyCacheMarker implements Serializable {
        public static final EmptyCacheMarker INSTANCE = new EmptyCacheMarker();
        private EmptyCacheMarker(){}
    }

    // 空值判断
    private boolean isEmptyMarker(Object obj) {
        return obj instanceof EmptyCacheMarker;
    }
}