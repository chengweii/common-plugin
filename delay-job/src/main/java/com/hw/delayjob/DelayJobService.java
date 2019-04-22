package com.hw.delayjob;

import java.util.concurrent.TimeUnit;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/18
 */
public interface DelayJobService {
    void execute(String jobKey, long delay, TimeUnit timeUnit, Runnable job);
}
