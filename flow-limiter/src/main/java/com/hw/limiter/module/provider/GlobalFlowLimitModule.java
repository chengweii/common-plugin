package com.hw.limiter.module.provider;

import com.hw.limiter.FlowLimiter;
import com.hw.limiter.module.FlowLimiterModule;
import org.springframework.stereotype.Service;

/**
 * 全局流量限流模块
 *
 * @author chengwei11
 * @date 2019/2/13
 */
@Service
public class GlobalFlowLimitModule implements FlowLimiterModule {
    @Override
    public boolean check(FlowLimiter.LimitResource limitResource) {
        return false;
    }

    @Override
    public Integer getOrder() {
        return 3;
    }
}
