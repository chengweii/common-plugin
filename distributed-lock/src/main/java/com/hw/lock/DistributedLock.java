package com.hw.lock;

/**
 * 分布式锁
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface DistributedLock {
    /**
     * 尝试获取锁
     *
     * @param lockKey  锁的主键
     * @param timeout  锁超时时间
     * @param lockMode 锁的类型
     * @return 锁的解锁密钥
     */
    String lock(String lockKey, int timeout, LockMode lockMode);

    /**
     * 解锁
     *
     * @param lockKey   锁的主键
     * @param secretKey 锁的解锁密钥
     * @return 是否解锁成功
     */
    boolean unlock(String lockKey, String secretKey);

    /**
     * 锁定执行操作
     *
     * @param action   锁定操作
     * @param timeout  锁超时时间
     * @param lockMode 锁的类型
     * @param lockKey  锁的主键
     * @param <R>      返回结果类型
     * @return 操作结果
     */
    <R> Result<R> lock(String lockKey, int timeout, LockMode lockMode, LockAction<R> action) throws Throwable;

    /**
     * 锁定动作
     */
    @FunctionalInterface
    interface LockAction<R> {
        /**
         * 执行锁定动作
         *
         * @return 动作执行结果
         */
        Result<R> execute();
    }

    class Result<T> {
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
            return new Result(LOCK_SUCCESS, null, result);
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
            return new Result(code, message, result);
        }

        /**
         * 组装锁定失败结果
         *
         * @param <T> 动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> failed() {
            return new Result(LOCK_FAILED, null, null);
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
    enum LockMode {
        /**
         * 等待锁（获取失败等待重试）
         */
        WAIT_LOCK,
        /**
         * 尝试锁（获取失败不重试）
         */
        TRY_LOCK;
    }
}
