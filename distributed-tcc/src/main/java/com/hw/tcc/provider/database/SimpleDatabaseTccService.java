package com.hw.tcc.provider.database;

import com.hw.tcc.config.TccConfig;
import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.Transaction;
import com.hw.tcc.serialize.TccSerializer;

import java.util.List;

/**
 * 基于数据库持久化的简单分布式补偿事务服务
 * 注意：单点运行环境可以不用加分布式锁，集群运行环境请务必加分布式锁保证并发安全。
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class SimpleDatabaseTccService extends DatabaseTccService {

    public SimpleDatabaseTccService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccSerializer, tccPersistenceService, tccConfig);
    }

    public SimpleDatabaseTccService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccPersistenceService, tccConfig);
    }

    @Override
    public void compensate() {
        List<Transaction> transactionList = tccPersistenceService.scan(tccConfig.getMaxCount());
        for (Transaction transaction : transactionList) {
            executeTccCompensateAction(transaction);
        }
    }

    @Override
    protected boolean lockTransactionForCompensate(Transaction transaction) {
        // 注意：集群运行环境请重写此方法，务必加分布式锁保证并发安全。
        return true;
    }

    @Override
    protected void unlockTransactionForCompensate(Transaction transaction) {
        // 注意：集群运行环境请重写此方法，务必加分布式锁保证并发安全。
    }
}
