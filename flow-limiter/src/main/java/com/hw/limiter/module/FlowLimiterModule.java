package com.hw.limiter.module;

import com.hw.limiter.FlowLimiter;

/**
 * 限流器模块
 *
 * @author chengwei11
 * @date 2019/2/13
 */
public interface FlowLimiterModule {
    /**
     * 检查资源访问是否被限制
     *
     * @param limitResource 资源信息
     * @return 是否被限制
     */
    boolean check(FlowLimiter.LimitResource limitResource);

    /**
     * 获取限流器模块的处理顺序
     *
     * @return
     */
    Integer getOrder();
}
