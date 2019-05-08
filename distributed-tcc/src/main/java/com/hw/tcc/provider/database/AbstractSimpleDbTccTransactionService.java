package com.hw.tcc.provider.database;

import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.persistence.TccPersistenceService;
import com.hw.tcc.core.persistence.TccTransaction;
import com.hw.tcc.core.serialize.TccSerializer;

import java.util.List;

/**
 * 基于数据库持久化的简单分布式补偿事务服务
 * 注意：单点运行环境可以不用加分布式锁，集群运行环境请务必加分布式锁保证并发安全。
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public abstract class AbstractSimpleDbTccTransactionService extends AbstractDbTccTransactionService {

    public AbstractSimpleDbTccTransactionService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccSerializer, tccPersistenceService, tccConfig);
    }

    public AbstractSimpleDbTccTransactionService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccPersistenceService, tccConfig);
    }

    @Override
    public void compensate() {
        List<TccTransaction> transactionList = tccPersistenceService.scan();

        if (transactionList == null || transactionList.size() == 0) {
            return;
        }

        for (TccTransaction transaction : transactionList) {
            executeTccCompensateAction(transaction);
            if (isRetryInCurrentPeriod(transaction.getNextAt().getTime())) {
                executeTccCompensateAction(transaction);
            }
        }
    }
}
