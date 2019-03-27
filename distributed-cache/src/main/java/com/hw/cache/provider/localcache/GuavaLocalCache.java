package com.hw.cache.provider.localcache;

import com.hw.cache.LocalCache;

/**
 * Guava本地缓存
 *
 * @author chengwei11
 * @date 2019/3/27
 */
public class GuavaLocalCache implements LocalCache {

    @Override
    public <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction) {
        return null;
    }

    @Override
    public <R> R get(String group, String cacheKey, long expire) {
        return null;
    }
}
