package com.hw.cache.provider.cacheaside;

import com.hw.cache.CacheAsideCache;
import com.hw.cache.serialize.CacheSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于Redis的旁路缓存服务
 * 注意：并发获取缓存源数据（查库）时，目前通过本地锁进行的控制，如果集群服务器不是很多的情况，本地锁控制已经足以应付；
 * 另外各集群服务器的网络负载等情况不一定相同，不使用分布式锁控制也可以一定程度上缓解单台服务器问题造成的单点问题。
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class RedisCacheAsideCache implements CacheAsideCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisCacheAsideCache.class);

    private static final String NX = "NX";
    private static final String PX = "PX";

    protected Jedis jedis;
    protected CacheSerializer cacheSerializer;
    protected int daoActionTimeout;

    private final ReentrantLock lock = new ReentrantLock();

    public RedisCacheAsideCache(Jedis jedis, CacheSerializer cacheSerializer, int daoActionTimeout) {
        this.jedis = jedis;
        this.cacheSerializer = cacheSerializer;
        this.daoActionTimeout = daoActionTimeout;
    }

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        String deserializeData = jedis.get(cacheKey);
        if (deserializeData != null) {
            ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
            return result.getResult();
        }

        return reload(group, cacheKey, expire, param, daoAction);
    }

    protected <P, R> R reload(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        try {
            if (lock.tryLock(daoActionTimeout, TimeUnit.MILLISECONDS)) {
                String deserializeData = jedis.get(cacheKey);
                if (deserializeData != null) {
                    ResultWrapper<P, R> result = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);
                    return result.getResult();
                }

                R result = daoAction.execute(param);
                ResultWrapper<P, R> resultWrapper = new ResultWrapper<P, R>(param, result, expire, daoAction.getClass().getName(), group);
                String serializeData = cacheSerializer.serialize(resultWrapper);
                jedis.set(cacheKey, serializeData, NX, PX, expire);

                return result;
            }
        } catch (InterruptedException t) {
            LOGGER.error("获取缓存源数据时锁定失败：cacheKey={}", cacheKey, t);
        } finally {
            after(group, cacheKey, expire, param, daoAction);
            lock.unlock();
        }

        return null;
    }

    protected <P, R> void after(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
    }
}
