package com.hw.external.cache.provider.cacheaside;

import com.hw.external.cache.CacheAsideCache;
import com.hw.external.cache.provider.localcache.GuavaLocalCache;
import com.hw.external.cache.serialize.CacheSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * 基于Redis的旁路缓存服务
 * 注意：并发获取缓存源数据（查库）时，通过锁进行控制,避免重新加载源数据压力过大情况。
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class RedisCacheAsideCache implements CacheAsideCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisCacheAsideCache.class);

    private static final String OK = "OK";
    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String PX = "PX";

    private final static int MAX_TIME_INTERVAL = 100;
    private final static String CACHE_LOAD_LOCK_KEY = "CACHE_LOAD_LOCK_KEY_";

    protected Jedis jedis;
    protected CacheSerializer cacheSerializer;
    protected int daoActionTimeout;

    private GuavaLocalCache guavaLocalCache = new GuavaLocalCache();

    public RedisCacheAsideCache(Jedis jedis, CacheSerializer cacheSerializer, int daoActionTimeout) {
        this.jedis = jedis;
        this.cacheSerializer = cacheSerializer;
        this.daoActionTimeout = daoActionTimeout;
    }

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, boolean enableLocalCache, P param, DaoAction<P, R> daoAction) {
        if (enableLocalCache) {
            return guavaLocalCache.get(group, cacheKey, expire, param, (args) -> {
                return get(group, cacheKey, expire, args, daoAction);
            });
        }

        return get(group, cacheKey, expire, param, daoAction);
    }

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        String deserializeData = jedis.get(cacheKey);
        if (deserializeData != null) {
            ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
            return result.getResult();
        }

        // 无效的缓存KEY直接返回空值结果，且不再缓存空值结果，避免无效KEY恶意攻击造成的缓存溢出甚至宕机情况。
        if (isInvalidKey(group, cacheKey)) {
            return null;
        }

        return load(group, cacheKey, expire, param, daoAction, false);
    }

    @Override
    public <R> R get(String group, String cacheKey, long expire) {
        return null;
    }

    @Override
    public boolean isInvalidKey(String group, String cacheKey) {
        return false;
    }

    protected <P, R> R load(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction, boolean forceUpdate) {
        boolean loadSuccess = false;
        // 获取缓存时增加分布式锁避免多线程访问时造成的缓存击穿情况
        String secretKey = lock(cacheKey, daoActionTimeout);

        try {
            if (secretKey == null) {
                return null;
            }

            if (!forceUpdate) {
                String deserializeData = jedis.get(cacheKey);
                if (deserializeData != null) {
                    ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
                    return result.getResult();
                }
            }

            R result = daoAction.execute(param);
            ResultWrapper<P, R> resultWrapper = new ResultWrapper<P, R>(param, result, expire, daoAction.getClass().getName(), group);
            String serializeData = cacheSerializer.serialize(resultWrapper);
            jedis.set(cacheKey, serializeData, NX, PX, expire);

            loadSuccess = true;

            return result;
        } catch (Throwable t) {
            LOGGER.error("获取缓存源数据时锁定失败：cacheKey={}", cacheKey, t);
        } finally {
            after(group, cacheKey, expire, param, daoAction, loadSuccess);
            unlock(cacheKey, secretKey);
        }

        return null;
    }

    private String lock(String lockKey, int timeout) {
        String secretKey = UUID.randomUUID().toString();
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end) {
            if (OK.equals(jedis.set(CACHE_LOAD_LOCK_KEY + lockKey, secretKey, NX, PX, timeout))) {
                return secretKey;
            }
            try {
                Thread.sleep(Double.valueOf(Math.random() * MAX_TIME_INTERVAL).longValue());
            } catch (InterruptedException e) {
                LOGGER.error("获取缓存源数据时锁定等待失败：lockKey={}", lockKey, e);
            }
        }
        return null;
    }

    private void unlock(String lockKey, String secretKey) {
        if (secretKey == null) {
            return;
        }

        if (secretKey.equals(jedis.get(CACHE_LOAD_LOCK_KEY + lockKey))) {
            jedis.del(CACHE_LOAD_LOCK_KEY + lockKey);
        }
    }

    protected <P, R> void after(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction, boolean loadSuccess) {
    }
}
