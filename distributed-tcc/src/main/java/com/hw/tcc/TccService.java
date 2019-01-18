package com.hw.tcc;

/**
 * 分布式补偿事务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccService {

    /**
     * 执行补偿事务
     *
     * @param transactionId       事务ID
     * @param transactionData     事务依赖数据对象
     * @param compensateActionClz 事务补偿动作类型
     * @param transactionAction   事务动作
     * @param <T>                 事务动作返回结果类型
     * @param <R>                 事务依赖数据对象类型
     * @return 事务动作返回结果
     */
    <T, R> Result<T> execute(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) throws Throwable;

    /**
     * 补偿失败事务（执行补偿动作）
     *
     * @param maxCount      执行补偿动作的最大数量
     * @param maxRetryTimes 执行补偿动作的最大重试次数
     */
    void compensate(int maxCount, int maxRetryTimes);

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
        /**
         * 是否要补偿
         */
        private boolean isCompensate;

        private static final int TCC_SUCCESS = 200;
        private static final int TCC_FAILED = 404;

        private Result(int code, String message, T result, boolean isCompensate) {
            this.code = code;
            this.message = message;
            this.result = result;
            this.isCompensate = isCompensate;
        }

        /**
         * 组装事务成功结果
         *
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> success(T result) {
            return new Result(TCC_SUCCESS, null, result, false);
        }

        /**
         * 组装事务成功结果
         *
         * @param code   动作执行结果响应码
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> success(int code, String message, T result) {
            return new Result(code, message, result, false);
        }

        /**
         * 组装事务补偿结果（非异常场景手动执行补偿）
         *
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> compensate(T result) {
            return new Result(TCC_SUCCESS, null, result, true);
        }

        /**
         * 组装事务补偿结果（非异常场景手动执行补偿）
         *
         * @param code   动作执行结果响应码
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> compensate(int code, String message, T result) {
            return new Result(code, message, result, true);
        }

        /**
         * @return 事务是否补偿
         */
        public boolean isCompensate() {
            return isCompensate;
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

    interface TransactionAction<T> {
        Result<T> execute();
    }

}
