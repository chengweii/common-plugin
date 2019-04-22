package com.hw.tcc.core;

import com.hw.tcc.core.compensate.TccCompensateAction;

/**
 * 分布式补偿事务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccTransactionService {
    /**
     * 执行补偿事务
     *
     * @param transactionId       事务ID：用于支持补偿动作重试时的业务去重和幂等
     * @param transactionData     事务依赖数据对象
     * @param compensateActionClz 事务补偿动作类型
     * @param transactionAction   事务动作
     * @param <T>                 事务动作返回结果类型
     * @param <R>                 事务依赖数据对象类型
     * @return 事务动作返回结果
     */
    <T, R> Result<T> execute(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction);

    /**
     * 执行补偿事务
     * 注意：事务补偿动作类型默认为当前类，所以当前类必须实现
     *
     * @param transactionId     事务ID：用于支持补偿动作重试时的业务去重和幂等
     * @param transactionData   事务依赖数据对象
     * @param transactionAction 事务动作
     * @param <T>               事务动作返回结果类型
     * @param <R>               事务依赖数据对象类型
     * @return 事务动作返回结果
     */
    <T, R> Result<T> execute(String transactionId, R transactionData, TransactionAction<T> transactionAction);

    /**
     * 补偿失败事务（执行补偿动作）
     */
    void compensate();

    /**
     * 分布式补偿事务执行结果
     *
     * @param <T>
     */
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
            return new Result<T>(TCC_SUCCESS, null, result, false);
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
            return new Result<T>(code, message, result, false);
        }

        /**
         * 组装事务补偿结果（非异常场景手动执行补偿）
         *
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> failed(T result) {
            return new Result<T>(TCC_FAILED, null, result, true);
        }

        /**
         * 组装事务补偿结果（非异常场景手动执行补偿）
         *
         * @param code   动作执行结果响应码
         * @param result 动作执行结果
         * @param <T>    动作执行结果类型
         * @return 锁定结果
         */
        public static <T> Result<T> failed(int code, String message, T result) {
            return new Result<T>(code, message, result, true);
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

    /**
     * 分布式事务控制动作
     *
     * @param <T>
     */
    @FunctionalInterface
    interface TransactionAction<T> {
        /**
         * 执行分布式事务控制动作
         *
         * @return 执行结果
         */
        Result<T> execute();
    }
}
