package com.hw.cache.provider.autorefresh;

import com.hw.cache.ExpiredAutoRefreshCache;
import com.hw.cache.provider.cacheaside.RedisCacheAsideCache;
import com.hw.cache.serialize.CacheSerializer;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的过期自动更新型缓存服务
 *
 * @author chengwei11
 * @date 2019/2/2
 */
public class RedisExpiredAutoRefreshCache extends RedisCacheAsideCache implements ExpiredAutoRefreshCache, InitializingBean {
    /**
     * 缓存刷新提前时间
     */
    private int cacheRefreshAdvanceTime;
    /**
     * 缓存刷新扫描周期时间
     */
    private int cacheRefreshPeriodTime;
    /**
     * 缓存过期时间记录过期时间
     */
    private int cacheRefreshRecordsExpireTime;

    private static final String REDIS_EXPIRED_AUTO_REFRESH_CACHE_KEY = "REDIS_EXPIRED_AUTO_REFRESH_CACHE_KEY";

    private ScheduledExecutorService refreshExecutor;

    @Resource
    private List<DaoAction> daoActions;

    public RedisExpiredAutoRefreshCache(Jedis jedis, CacheSerializer cacheSerializer, int daoActionTimeout, int cacheRefreshAdvanceTime, int cacheRefreshPeriodTime) {
        super(jedis, cacheSerializer, daoActionTimeout);
        this.cacheRefreshAdvanceTime = cacheRefreshAdvanceTime;
        this.cacheRefreshPeriodTime = cacheRefreshPeriodTime;
        this.cacheRefreshRecordsExpireTime = cacheRefreshPeriodTime * 2;
        refreshExecutor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        return super.get(group, cacheKey, expire, param, daoAction);
    }

    @Override
    protected <P, R> void after(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        jedis.zadd(REDIS_EXPIRED_AUTO_REFRESH_CACHE_KEY, expire, cacheKey);
        jedis.expire(REDIS_EXPIRED_AUTO_REFRESH_CACHE_KEY, cacheRefreshRecordsExpireTime);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (daoActions == null || daoActions.size() == 0) {
            return;
        }

        refreshExecutor.scheduleAtFixedRate(() -> {
            long start = System.currentTimeMillis();
            long end = start + cacheRefreshPeriodTime;

            Set<String> result = jedis.zrevrangeByScore(REDIS_EXPIRED_AUTO_REFRESH_CACHE_KEY, end, start);

            if (result == null || result.size() == 0) {
                return;
            }

            result.forEach(cacheKey -> {
                String deserializeData = jedis.get(cacheKey);
                if (deserializeData == null) {
                    return;
                }

                ResultWrapper resultWrapper = cacheSerializer.deserialize(deserializeData, ResultWrapper.class);

                String daoActionClzName = resultWrapper.getDaoActionClzName();

                Optional<DaoAction> daoAction = daoActions.stream().filter(action -> action.getClass().getName().equals(daoActionClzName)).findFirst();

                if (daoAction.isPresent()) {
                    reload(resultWrapper.getGroup(), cacheKey, resultWrapper.getExpireTime(), resultWrapper.getParam(), daoAction.get());
                }
            });

        }, 0, cacheRefreshPeriodTime, TimeUnit.MILLISECONDS);

    }
}
