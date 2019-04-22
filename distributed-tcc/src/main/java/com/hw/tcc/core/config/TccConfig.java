package com.hw.tcc.core.config;

import lombok.Data;

/**
 * 分布式事务补偿配置
 *
 * @author chengwei11
 * @date 2019/1/21
 */
@Data
public class TccConfig {
    /**
     * 事务补偿超时时间（毫秒）
     */
    private long transactionTimeout;
    /**
     * 分布式锁超时时间（毫秒）
     */
    private int lockTimeout;
    /**
     * 事务补偿服务单次扫描事务最大条数
     */
    private int maxCount;
    /**
     * 事务补偿重试最大次数
     */
    private int maxRetryTimes;
    /**
     * 事务补偿重试时间间隔 单位：秒
     * 例如：1, 2, 3, 1 * 60, 2 * 60, 4 * 60, 16 * 60, 256 * 60
     */
    private String delaySecondsList;
    /**
     * 事务补偿重试最大间隔时间
     */
    private Long maxDelaySeconds;

    private long tickDuration;


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
}

