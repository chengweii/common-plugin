package com.hw.cache;

/**
 * 过期自动更新型缓存服务
 * 实现：定时任务定期拉取将要过期的缓存执行更新动作。
 *
 * @author chengwei11
 * @date 2019/1/23
 */
public interface ExpiredAutoRefreshCache extends DistributedCache {
}
