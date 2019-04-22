package com.hw.tcc.core.lock;

/**
 * Tcc补偿执行分布式锁服务
 *
 * @author chengwei11
 * @date 2019/4/12
 */
public interface TccLockService {
    /**
     * 分布式锁
     *
     * @param lockKey 锁的主键
     * @param timeout 锁的超时时间 ()
     * @param action  锁的动作
     */
    void lock(String lockKey, int timeout, LockAction action);

    /**
     * 锁定动作
     */
    @FunctionalInterface
    interface LockAction {
        /**
         * 执行锁定动作
         */
        void execute();
    }
}
