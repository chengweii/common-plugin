package com.hw.cache.provider;

import com.hw.cache.CacheAsideCache;
import com.hw.cache.serialize.CacheSerializer;
import redis.clients.jedis.Jedis;

/**
 * 描述信息
 *
 * @author chengwei11
 * @since 2019/1/31
 */
public class CacheAsideCacheImpl implements CacheAsideCache {
    private Jedis jedis;
    private CacheSerializer cacheSerializer;

    public CacheAsideCacheImpl(Jedis jedis, CacheSerializer cacheSerializer) {
        this.jedis = jedis;
        this.cacheSerializer = cacheSerializer;
    }

    public <P, R> R get(String cacheKey, int expire,P param, DaoAction<R> daoAction) {
        String deserializeData = jedis.get(cacheKey);
        if (deserializeData != null) {
            ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
            return result.getResult();
        }

        R result = daoAction.execute();
        ResultWrapper<P, R> resultWrapper = new ResultWrapper<P, R>(null, result);
        String serializeData = cacheSerializer.serialize(resultWrapper);
        jedis.set(cacheKey, serializeData);
        jedis.expire(cacheKey, expire);

        return result;
    }

    @FunctionalInterface
    interface DaoAction<R> {
        R execute();
    }

}
