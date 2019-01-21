package com.hw.tcc.config;

/**
 * 分布式事务补偿
 *
 * @author chengwei11
 * @date 2019/1/21
 */
public class TccConfig {
    /**
     * 并行补偿服务线程池线程最大线程数
     */
    private int maximumPoolSize;
    /**
     * 并行补偿服务线程池线程最大存活时间（毫秒）
     */
    private long keepAliveTime;
    /**
     * 并行补偿服务线程池队列最大容量
     */
    private int queueCapacity;
    /**
     * 事务补偿超时时间（毫秒）
     */
    private long transactionTimeout;
    /**
     * 事务补偿服务单次扫描事务最大条数
     */
    private int maxCount;
    /**
     * 事务补偿重试最大次数
     */
    private int maxRetryTimes;

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public long getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(long transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    @Override
    public String toString() {
        return "TccConfig{" +
                "maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", queueCapacity=" + queueCapacity +
                ", transactionTimeout=" + transactionTimeout +
                ", maxCount=" + maxCount +
                ", maxRetryTimes=" + maxRetryTimes +
                '}';
    }
}
