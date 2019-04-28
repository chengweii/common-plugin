package com.hw.plugins.limiter;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 系统限流器
 *
 * @author chengwei11
 * @date 2019/4/26
 */
public class SystemLimiterImpl implements SystemLimiter {
    private final static Logger LOGGER = LoggerFactory.getLogger(SystemLimiterImpl.class);

    private ConcurrentMap<String, RateLimiter> resourceLimiterMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, Long> resourceQpsMap = Maps.newConcurrentMap();

    @Override
    public void init(Map<String, Long> resourceQpsConfig) {
        resourceQpsMap.putAll(resourceQpsConfig);
    }

    @Override
    public boolean request(String resourceKey) {
        RateLimiter rateLimiter = resourceLimiterMap.get(resourceKey);

        Long qps = resourceQpsMap.get(resourceKey);
        // 未设置QPS则不限流
        if (qps == null || qps == 0L) {
            return false;
        }

        if (rateLimiter == null) {
            RateLimiter resourceRateLimiter = RateLimiter.create(qps);
            RateLimiter putResourceRateLimiter = resourceLimiterMap.putIfAbsent(resourceKey, resourceRateLimiter);
            if (putResourceRateLimiter != null) {
                rateLimiter = putResourceRateLimiter;
            }
        }
        rateLimiter.setRate(qps);

        // 非阻塞请求令牌
        if (!rateLimiter.tryAcquire()) {
            // 资源访问触发限流
            LOGGER.info("资源访问触发限流：resourceKey={}，qps={}", resourceKey, qps);
            return true;
        }

        return false;
    }
}
