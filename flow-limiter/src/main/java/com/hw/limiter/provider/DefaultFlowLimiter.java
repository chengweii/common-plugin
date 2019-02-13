package com.hw.limiter.provider;

import com.hw.limiter.FlowLimiter;
import com.hw.limiter.module.FlowLimiterModule;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * 默认限流器
 *
 * @author chengwei11
 * @date 2019/2/12
 */
public class DefaultFlowLimiter implements FlowLimiter, InitializingBean {
    private LimitConfig limitConfig;

    @Resource
    private List<FlowLimiterModule> flowLimiterModuleList;

    public DefaultFlowLimiter(LimitConfig limitConfig) {
        this.limitConfig = limitConfig;
    }

    @Override
    public boolean check(LimitResource limitResource) {
        if (flowLimiterModuleList == null || flowLimiterModuleList.size() == 0) {
            return false;
        }

        for (FlowLimiterModule flowLimiterModule : flowLimiterModuleList) {
            boolean result = flowLimiterModule.check(limitResource);
            if (result) {
                return true;
            }
        }

        return false;
    }

    @Override
    public LimitConfig getLimitConfig() {
        return this.limitConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (flowLimiterModuleList == null || flowLimiterModuleList.size() == 0) {
            return;
        }

        flowLimiterModuleList.sort(new Comparator<FlowLimiterModule>() {
            @Override
            public int compare(FlowLimiterModule o1, FlowLimiterModule o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });
    }
}
