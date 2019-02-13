package com.hw.limiter.module.provider;

import com.hw.limiter.FlowLimiter;
import com.hw.limiter.module.FlowLimiterModule;

/**
 * 热点参数限流模块
 *
 * @author chengwei11
 * @date 2019/2/13
 */
public class HotParamLimitModule implements FlowLimiterModule {
    @Override
    public boolean check(FlowLimiter.LimitResource limitResource) {
        return false;
    }

    @Override
    public Integer getOrder() {
        return 4;
    }
}
