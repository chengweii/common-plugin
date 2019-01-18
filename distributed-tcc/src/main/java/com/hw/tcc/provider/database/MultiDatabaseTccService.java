package com.hw.tcc.provider.database;

import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.Transaction;
import com.hw.tcc.serialize.TccSerializer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于数据库持久化的简单分布式补偿事务服务
 * 注意：单点运行环境可以不用加分布式锁，集群运行环境请务必加分布式锁保证并发安全。
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class MultiDatabaseTccService extends DatabaseTccService {
    private ExecutorService executeTccCompensateActionExecutor = null;

    public MultiDatabaseTccService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService, int maximumPoolSize) {
        super(tccSerializer, tccPersistenceService);
        executeTccCompensateActionExecutor = new ThreadPoolExecutor(1, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(8493), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void compensate(int maxCount, int maxRetryTimes) {
        List<Transaction> transactionList = tccPersistenceService.scan(maxCount);
        for (Transaction transaction : transactionList) {
            if (transaction.getRetryTimes() > maxRetryTimes) {
                continue;
            }

            executeTccCompensateActionExecutor.submit(() -> {
                executeTccCompensateAction(transaction);
            });
        }
    }

    @Override
    protected boolean lockTransactionForCompensate(Transaction transaction) {
        return true;
    }

    @Override
    protected void unlockTransactionForCompensate(Transaction transaction) {
    }
}
