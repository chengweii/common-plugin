package com.hw.tcc;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * Redis分布式锁服务
 *
 * @author chengwei11
 * @date 2019/4/3
 */
@Service
public class RedisLockService {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockService.class);
    /**
     * 重试获取锁时单次等待的最大时间间隔
     */
    private final static int MAX_TIME_INTERVAL = 100;

    private static final String OK = "OK";
    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String PX = "PX";

    /**
     * 尝试获取锁
     *
     * @param lockKey  锁的主键
     * @param timeout  锁超时时间 单位：毫秒
     * @param lockMode 锁的类型
     * @return 锁的解锁密钥
     */
    public String lock(String lockKey, int timeout, LockMode lockMode) {
        if (Strings.isNullOrEmpty(lockKey) || timeout <= 0 || lockMode == null) {
            LOGGER.error("锁定失败:lockKey={},timeout={},lockMode={}", lockKey, timeout, lockMode);
            return null;
        }
        String secretKey = UUID.randomUUID().toString();
        try {
            if (lockMode == LockMode.FAIL_OVER) {
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
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            if (OK.equals(redisClient.set(lockKey, secretKey, NX, PX, timeout))) {
                return secretKey;
            }
            return null;
        } finally {
            redisClient.close();
        }
    }

    private String waitLock(String lockKey, String secretKey, int timeout) throws Throwable {
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end) {
            Jedis redisClient = JedisClient.getJedisClient();
            try {
                if (OK.equals(redisClient.set(lockKey, secretKey, NX, PX, timeout))) {
                    return secretKey;
                }
            } finally {
                redisClient.close();
            }
            Thread.sleep(Double.valueOf(Math.random() * MAX_TIME_INTERVAL).longValue());
        }
        return null;
    }

    /**
     * 解锁
     *
     * @param lockKey   锁的主键
     * @param secretKey 锁的解锁密钥
     * @return 是否解锁成功
     */
    public boolean unlock(String lockKey, String secretKey) {
        if (Strings.isNullOrEmpty(lockKey) || Strings.isNullOrEmpty(secretKey)) {
            LOGGER.error("解锁失败:lockKey={},secretKey={}", lockKey, secretKey);
            return false;
        }
        try {
            Jedis redisClient = JedisClient.getJedisClient();
            try {
                if (secretKey.equals(redisClient.get(lockKey))) {
                    redisClient.del(lockKey);
                    return true;
                }
            } finally {
                redisClient.close();
            }
        } catch (Exception e) {
            LOGGER.error("解锁失败:lockKey={},secretKey={}", lockKey, secretKey, e);
        }
        return false;
    }

    /**
     * 锁定执行操作
     *
     * @param action   锁定操作
     * @param timeout  锁超时时间 单位：毫秒
     * @param lockMode 锁的类型
     * @param lockKey  锁的主键
     * @param <R>      返回结果类型
     * @return 操作结果
     */
    public <R> Result<R> lock(String lockKey, int timeout, LockMode lockMode, LockAction<R> action) {
        String secretKey = lock(lockKey, timeout, lockMode);
        if (Strings.isNullOrEmpty(secretKey)) {
            return Result.failed();
        }
        try {
            Result<R> result = action.execute();
            if (result == null) {
                return Result.success(null);
            }
            return result;
        } catch (Throwable t) {
            throw t;
        } finally {
            unlock(lockKey, secretKey);
        }
    }

    /**
     * 锁定动作
     */
    @FunctionalInterface
    public interface LockAction<R> {
        /**
         * 执行锁定动作
         *
         * @return 动作执行结果
         */
        Result<R> execute();
    }

    public static class Result<T> {
        /**
         * 响应码
         */
        private int code;
        /**
         * 响应消息
         */
        private String message;
        /**
         * 响应结果
         */
        private T result;

        private static final int LOCK_SUCCESS = 200;
        private static final int LOCK_FAILED = 404;

        private Result(int code, String message, T result) {
            this.code = code;
            this.message = message;
            this.result = result;
        }

        /**
         * 组装锁定成功结果
         *
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> success(T result) {
            return new Result<T>(LOCK_SUCCESS, null, result);
        }

        /**
         * 组装锁定成功结果
         *
         * @param code   动作执行结果响应码
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> success(int code, String message, T result) {
            return new Result<T>(code, message, result);
        }

        /**
         * 组装锁定失败结果
         *
         * @param <T> 动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> failed() {
            return new Result<T>(LOCK_FAILED, null, null);
        }

        /**
         * 获取锁定是否成功
         *
         * @return 是否锁定成功
         */
        public boolean isSuccess() {
            return this.code == LOCK_SUCCESS;
        }

        public T getResult() {
            return result;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 锁类型
     */
    public enum LockMode {
        /**
         * 失败重试型（获取锁失败重试，可能会带来延迟）
         */
        FAIL_OVER,
        /**
         * 快速失败型（获取锁失败不重试）
         */
        FAIL_FAST;
    }
}
