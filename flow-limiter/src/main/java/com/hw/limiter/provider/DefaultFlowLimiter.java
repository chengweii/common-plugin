package com.hw.limiter.provider;

import com.hw.limiter.FlowLimiter;

/**
 * 默认限流器
 *
 * @author chengwei11
 * @date 2019/2/12
 */
public class DefaultFlowLimiter implements FlowLimiter {
    private Config config;

    public DefaultFlowLimiter(Config config){
        this.config = config;
    }

    public boolean check(Resource resource) {
        return false;
    }

    public Config getConfig() {
        return this.config;
    }
}
