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

    public <P, R> R get(String cacheKey, int expire, P queryParam, DaoAction<R> daoAction) {
        String deserializeData = jedis.get(cacheKey);
        if (deserializeData != null) {
            ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
            return result.result;
        }

        R result = daoAction.execute();
        ResultWrapper<P, R> resultWrapper = new ResultWrapper<P, R>(queryParam, result);
        String serializeData = cacheSerializer.serialize(resultWrapper);
        jedis.set(cacheKey, serializeData);
        jedis.expire(cacheKey, expire);

        return result;
    }

    @FunctionalInterface
    interface DaoAction<R> {
        R execute();
    }

    static class ResultWrapper<P, R> {
        private P queryParam;
        private R result;

        public ResultWrapper(P queryParam, R result) {
            this.queryParam = queryParam;
            this.result = result;
        }

        public P getQueryParam() {
            return queryParam;
        }

        public R getResult() {
            return result;
        }
    }
}
