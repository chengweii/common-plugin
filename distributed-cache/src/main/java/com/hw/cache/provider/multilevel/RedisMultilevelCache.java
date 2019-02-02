package com.hw.cache.provider.multilevel;

import com.hw.cache.MultilevelCache;
import com.hw.cache.provider.cacheaside.RedisCacheAsideCache;
import com.hw.cache.serialize.CacheSerializer;
import redis.clients.jedis.Jedis;

/**
 * 基于Redis的多级缓存服务
 *
 * @author chengwei11
 * @date 2019/2/2
 */
public class RedisMultilevelCache extends RedisCacheAsideCache implements MultilevelCache {
    public RedisMultilevelCache(Jedis jedis, CacheSerializer cacheSerializer, int daoActionTimeout) {
        super(jedis, cacheSerializer, daoActionTimeout);
    }

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        return super.get(group, cacheKey, expire, param, daoAction);
    }
}
