package com.hw.lock.provider.shark;

import com.hw.lock.DistributedLock;

/**
 * 基于SharkDB的分布式锁
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class SharkDistributedLock implements DistributedLock {
    @Override
    public String lock(String lockKey, int timeout, LockMode lockMode) {
        return null;
    }

    @Override
    public boolean unlock(String lockKey, String secretKey) {
        return false;
    }

    @Override
    public <R> Result<R> lock(String lockKey, int timeout, LockMode lockMode, LockAction<R> action) throws Throwable {
        return null;
    }
}
