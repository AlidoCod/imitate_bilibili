package org.demo.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 高并发解决方案
 * 缓存穿透，缓存击穿解决方案
 * 自定义缓存组件，抛弃Spring-Cache, 自定义就可以不用学框架啦
 */
@Slf4j
@Component
public class RedisClient {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final ThreadPoolExecutor threadPoolExecutor;

    private static final long CACHE_NULL_TTL = 10;

    private static final String LOCK_KEY_PREFIX = "LOCK:";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RedisClient(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, ThreadPoolExecutor threadPoolExecutor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * 设置永久Key-Value
     */
    private <T> void set(String keyPrefix, String keySuffix, @NotNull T value) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(keyPrefix + keySuffix, getJsonString(value));
    }

    /**
     * 设置会过期的key-value
     */
    public <T> void setWithExpiredTime(String keyPrefix, String keySuffix, @NotNull T value, Long time, TimeUnit unit) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(keyPrefix + keySuffix, getJsonString(value), time, unit);
    }

    /**
     * 设置仅能设置一次的缓存
     */
    public <T> boolean setIfAbsent(String keyPrefix, String keySuffix, @NotNull T value, Long time, TimeUnit unit) throws JsonProcessingException {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(keyPrefix + keySuffix, getJsonString(value), time, unit));
    }

    /**
     * 避免String对象转化Json时发生异常
     */
    private <T> String getJsonString(@NotNull T value) throws JsonProcessingException {
        return value.getClass() == String.class ? (String) value : objectMapper.writeValueAsString(value);
    }

    /**
     * 普通的get方法，不会自动重建缓存。
     */
    public <T> T get(String keyPrefix, String keySuffix, @NotNull Class<T> clazz) throws JsonProcessingException {
        String value = stringRedisTemplate.opsForValue().get(keyPrefix + keySuffix);
        if (value == null)
            return null;
        return readValue(value, clazz);
    }

    private <T> T readValue(String value, Class<T> clazz) throws JsonProcessingException {
        return clazz == String.class ? (T) value : objectMapper.readValue(value, clazz);
    }

    /**
     * 获取缓存，若缓存不存在则重建缓存，此方法缓存空值
     */
    public <V> V getWithNonHotSpotKey(
            String keyPrefix, String keySuffix, Class<V> clazz, Callable<V> dbFallback, Long time, TimeUnit unit) throws Exception {
        return getWithNonHotSpotKey(keyPrefix, keySuffix, clazz, dbFallback, time, unit, true);
    }

    /**
     * 缓存不存在时会自动添加数据库数据进入缓存的非热点Key查询方式，解决了缓存穿透问题，未解决缓存击穿问题，也就是缓存Key对应的空Value
     * @param keyPrefix 自定义key前缀
     * @param keySuffix 自定义key
     * @param clazz value的类型
     * @param dbFallback 当缓存未命中时，查询数据库返回V的函数
     * @return 如果返回值未null，说明缓存不存在
     */
    public <V> V getWithNonHotSpotKey(
            String keyPrefix, String keySuffix, Class<V> clazz, Callable<V> dbFallback, Long time, TimeUnit unit, boolean cacheNullObject) throws Exception {
        // 从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(keyPrefix + keySuffix);
        // 若非空串存在
        if (StrUtil.isNotBlank(json)) {
            return readValue(json, clazz);
        }
        // 命中的是空串
        if (json != null) {
            // 返回一个错误信息
            return null;
        }

        // 不存在，根据id查询数据库
        V obj = dbFallback.call();
        // 不存在，返回错误
        if (obj == null && cacheNullObject) {
            // 将空值写入redis
            setWithExpiredTime(keyPrefix, (String) keySuffix, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 存在，写入redis
        this.setWithExpiredTime(keyPrefix, (String) keySuffix, obj, time, unit);
        return obj;
    }

    /**
     * 设置逻辑过期的key-value
     */
    public void setWithLogicalExpire(String keyPrefix, String keySuffix, Object value, Long time, TimeUnit unit) throws JsonProcessingException {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入Redis
        set(keyPrefix, keySuffix, redisData);
    }

    /**
     * 搭配setWithLogicalExpire使用，因此热点Key的缓存是一定存在的，在缓存过期后，就先返回过期数据，后进行缓存重建。
     * @param keyPrefix 自定义key前缀
     * @param keySuffix 自定义key
     * @param clazz value的类型
     * @param dbFallback 当缓存未命中时，查询数据库返回V的函数
     * @return 如果返回值未null，说明缓存不存在
     */
    public <K, V> V getWithHotSpotKey(
            String keyPrefix, K keySuffix, Class<V> clazz, Function<K, V> dbFallback, Long time, TimeUnit unit) throws JsonProcessingException {
        // 从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(keyPrefix + keySuffix);
        // 判断是否存在
        if (StrUtil.isBlank(json)) {
            // 不存在缓存的情况说明这不是热点Key或者热点Key的缓存仍未重建成功，那么就先提前返回，减轻服务器负担
            return null;
        }
        // 命中，需要先把json反序列化为对象
        RedisData redisData = objectMapper.readValue(json, RedisData.class);
        // 将Object对象转化为具体对象
        V obj = objectMapper.convertValue(redisData.getData(), clazz);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回缓存
            return obj;
        }
        // 已过期，需要缓存重建, 获取互斥锁
        boolean isLock = tryLock((String) keySuffix);
        // 判断是否获取锁成功
        if (isLock){
            // 成功，开启独立线程，实现缓存重建
            threadPoolExecutor.submit(() -> {
                try {
                    // 查询数据库
                    V object = dbFallback.apply(keySuffix);
                    // 重建缓存
                    this.setWithLogicalExpire(keyPrefix, (String) keySuffix, object, time, unit);
                }
                finally {
                    // 释放锁
                    unlock((String) keySuffix);
                }
                return null;
            });
        }
        // 返回过期缓存信息
        return obj;
    }


    public boolean delete(String keyPrefix, String keySuffix) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(keyPrefix + keySuffix));
    }

    private boolean tryLock(String keySuffix) throws JsonProcessingException {
        return setIfAbsent(LOCK_KEY_PREFIX, keySuffix, "1", 10L, TimeUnit.SECONDS);
    }

    private void unlock(String keySuffix) {
        stringRedisTemplate.delete(keySuffix);
    }

    @Data
    public static class RedisData {
        private LocalDateTime expireTime;
        private Object data;
    }

}
