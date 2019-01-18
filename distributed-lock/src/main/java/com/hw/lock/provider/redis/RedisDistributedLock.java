package com.hw.lock.provider.redis;

import com.google.common.base.Strings;
import com.hw.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * 基于Redis的分布式锁
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class RedisDistributedLock implements DistributedLock {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDistributedLock.class);

    private final static int MAX_TIME_INTERVAL = 100;

    private static final String OK = "OK";
    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String PX = "PX";

    private Jedis jedis;

    public RedisDistributedLock(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public String lock(String lockKey, int timeout, LockMode lockMode) {
        if (Strings.isNullOrEmpty(lockKey) || timeout <= 0 || lockMode == null) {
            LOGGER.error("锁定失败:lockKey={},timeout={},lockMode={}", lockKey, timeout, lockMode);
            return null;
        }
        String secretKey = UUID.randomUUID().toString();
        try {
            if (lockMode == LockMode.WAIT_LOCK) {
                return waitLock(lockKey, secretKey, timeout);
            } else {
                return tryLock(lockKey, secretKey, timeout);
            }
        } catch (Throwable e) {
            LOGGER.error("锁定失败:lockKey={},secretKey={},timeout={}", lockKey, secretKey, timeout, e);
        }
        return null;
    }

    private String tryLock(String lockKey, String secretKey, int timeout) {
        if (OK.equals(jedis.set(lockKey, secretKey, NX, PX, timeout))) {
            return secretKey;
        }
        return null;
    }

    private String waitLock(String lockKey, String secretKey, int timeout) throws Throwable {
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end) {
            if (OK.equals(jedis.set(lockKey, secretKey, NX, PX, timeout))) {
                return secretKey;
            }
            Thread.sleep(Double.valueOf(Math.random() * MAX_TIME_INTERVAL).longValue());
        }
        return null;
    }

    @Override
    public boolean unlock(String lockKey, String secretKey) {
        if (Strings.isNullOrEmpty(lockKey) || Strings.isNullOrEmpty(secretKey)) {
            LOGGER.error("解锁失败:lockKey={},secretKey={}", lockKey, secretKey);
            return false;
        }
        try {
            if (secretKey.equals(jedis.get(lockKey))) {
                jedis.del(lockKey);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("解锁失败:lockKey={},secretKey={}", lockKey, secretKey, e);
        }
        return false;
    }

    @Override
    public <R> Result<R> lock(String lockKey, int timeout, LockMode lockMode, LockAction<R> action) throws Throwable {
        String secretKey = lock(lockKey, timeout, lockMode);
        if (Strings.isNullOrEmpty(secretKey)) {
            return Result.failed();
        }
        try {
            return action.execute();
        } catch (Throwable t) {
            throw t;
        } finally {
            unlock(lockKey, secretKey);
        }
    }
}
