package com.hw.plugins.limiter;

import java.util.Map;

/**
 * 系统限流器
 *
 * @author chengwei11
 * @date 2019/4/26
 */
public interface SystemLimiter {
    public void init(Map<String, Long> resourceQpsConfig);

    public boolean request(String resourceKey);
}
