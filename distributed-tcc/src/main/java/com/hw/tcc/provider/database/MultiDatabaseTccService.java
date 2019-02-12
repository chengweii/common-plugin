package com.hw.tcc.provider.database;

import com.hw.tcc.config.TccConfig;
import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.TccTransaction;
import com.hw.tcc.serialize.TccSerializer;

import java.util.List;
import java.util.concurrent.*;

/**
 * 基于数据库持久化的并行分布式补偿事务服务
 * 注意：单点运行环境可以不用加分布式锁，集群运行环境请务必加分布式锁保证并发安全。
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class MultiDatabaseTccService extends DatabaseTccService {
    private ExecutorService executeTccCompensateActionExecutor = null;

    public MultiDatabaseTccService(TccSerializer tccSerializer,
                                   TccPersistenceService tccPersistenceService,
                                   TccConfig tccConfig) {
        super(tccSerializer, tccPersistenceService, tccConfig);
        this.init(tccConfig);
    }

    public MultiDatabaseTccService(TccPersistenceService tccPersistenceService,
                                   TccConfig tccConfig) {
        super(tccPersistenceService, tccConfig);
        this.init(tccConfig);
    }

    private void init(TccConfig tccConfig) {
        executeTccCompensateActionExecutor = new ThreadPoolExecutor(1, tccConfig.getMaximumPoolSize(), tccConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(tccConfig.getQueueCapacity()), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void compensate() {
        List<TccTransaction> transactionList = tccPersistenceService.scan(tccConfig.getMaxCount());

        if (transactionList == null || transactionList.size() == 0) {
            return;
        }

        for (TccTransaction transaction : transactionList) {
            Future result = executeTccCompensateActionExecutor.submit(() -> {
                executeTccCompensateAction(transaction);
            });

            try {
                result.get(tccConfig.getTransactionTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected boolean lockTransactionForCompensate(TccTransaction transaction) {
        // 集群运行环境请重写此方法，务必加分布式锁保证并发安全。
        return true;
    }

    @Override
    protected void unlockTransactionForCompensate(TccTransaction transaction) {
        // 集群运行环境请重写此方法，务必加分布式锁保证并发安全。
    }
}
