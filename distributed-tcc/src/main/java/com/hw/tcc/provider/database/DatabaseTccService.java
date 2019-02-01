package com.hw.tcc.provider.database;

import com.google.common.collect.Lists;
import com.hw.tcc.compensate.ActionSerialNoEnum;
import com.hw.tcc.compensate.TccCompensateAction;
import com.hw.tcc.compensate.TccTransactionData;
import com.hw.tcc.config.TccConfig;
import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.TccTransaction;
import com.hw.tcc.provider.BaseTccService;
import com.hw.tcc.serialize.TccSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 基于数据库持久化的分布式补偿事务服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public abstract class DatabaseTccService extends BaseTccService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseTccService.class);

    protected TccPersistenceService tccPersistenceService;

    public DatabaseTccService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccSerializer, tccConfig);
        this.tccPersistenceService = tccPersistenceService;
    }

    public DatabaseTccService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccConfig);
        this.tccPersistenceService = tccPersistenceService;
    }

    @Override
    public <T, R> Result<T> execute(ActionSerialNoEnum actionSerialNo, String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) throws Throwable {
        boolean isRetry = false;
        TccTransaction transaction = new TccTransaction();
        try {
            transaction.setActionSerialNo(actionSerialNo.name());
            transaction.setTransactionId(transactionId);
            String serializeData = tccSerializer.serialize(transactionData);
            transaction.setTransactionData(serializeData);
            transaction.setCompensateActionClz(compensateActionClz.getName());
            Date now = new Date();
            transaction.setExecuteAt(now);
            transaction.setNextAt(now);
            transaction.setRetryTimes(0);
            transaction.setStatus(TccTransaction.Status.EXECUTING.getValue());
            if (!tccPersistenceService.begin(transaction)) {
                throw new RuntimeException("分布式补偿事务开始失败！");
            }

            Result<T> result = transactionAction.execute();

            if (result.isCompensate()) {
                isRetry = true;
            }

            return result;
        } catch (Throwable e) {
            isRetry = true;
            throw e;
        } finally {
            if (isRetry) {
                transaction.setStatus(TccTransaction.Status.RETRYING.getValue());
                tccPersistenceService.retry(transaction);
            } else {
                tccPersistenceService.end(transaction);
            }
        }
    }

    /**
     * 执行补偿动作
     *
     * @param tccTransaction 事务实体
     */
    protected void executeTccCompensateAction(TccTransaction tccTransaction) {
        TccCompensateAction tccCompensateAction = getTccCompensateAction(tccTransaction.getCompensateActionClz());
        if (tccCompensateAction == null) {
            LOGGER.error("分布式补偿事务重试失败，没有找到对应的补偿动作对象：transaction={}", tccTransaction);
            return;
        }

        boolean isRetry = false;

        if (!lockTransactionForCompensate(tccTransaction)) {
            LOGGER.error("分布式补偿事务重试失败，当前事务已被锁定：transaction={}", tccTransaction);
            return;
        }

        try {
            try {
                TccTransactionData tccTransactionData = new TccTransactionData();
                tccTransactionData.setActionSerialNo(ActionSerialNoEnum.valueOf(tccTransaction.getActionSerialNo()));
                tccTransactionData.setSerializeData(tccTransaction.getTransactionData());
                tccTransactionData.setTransactionId(tccTransaction.getTransactionId());

                if (tccCompensateAction.execute(tccTransactionData)) {
                    LOGGER.info("分布式补偿事务重试成功：transactionId={}", tccTransaction.getTransactionId());
                } else {
                    isRetry = true;
                    LOGGER.error("分布式补偿事务重试失败：transactionId={}", tccTransaction.getTransactionId());
                }
            } catch (Throwable t) {
                isRetry = true;
                LOGGER.error("分布式补偿事务重试失败：transaction={}", tccTransaction, t);
            }

            tccTransaction.setRetryTimes(tccTransaction.getRetryTimes() + 1);
            tccTransaction.setExecuteAt(new Date());

            if (tccTransaction.getRetryTimes() >= tccConfig.getMaxRetryTimes()) {
                LOGGER.info("分布式补偿事务重试终止：transactionId={}", tccTransaction.getTransactionId());
                tccTransaction.setStatus(TccTransaction.Status.FAILED.getValue());
                tccPersistenceService.fail(tccTransaction);
                return;
            }

            if (isRetry) {
                Date nextExecuteTime = getNextExecuteTime(tccTransaction);
                tccTransaction.setNextAt(nextExecuteTime);
                tccTransaction.setStatus(TccTransaction.Status.RETRYING.getValue());
                tccPersistenceService.retry(tccTransaction);
            } else {
                tccPersistenceService.end(tccTransaction);
            }
        } finally {
            unlockTransactionForCompensate(tccTransaction);
        }
    }

    /**
     * 延迟重试时间
     */
    private static final List<Integer> DELAY_SECONDS_LIST = Lists.newArrayList(1, 2, 3, 1 * 60, 2 * 60, 4 * 60, 16 * 60, 256 * 60);

    @Override
    protected Date getNextExecuteTime(TccTransaction transaction) {
        Integer delaySeconds = DELAY_SECONDS_LIST.get(DELAY_SECONDS_LIST.size() - 1);
        if (transaction.getRetryTimes() <= DELAY_SECONDS_LIST.size()) {
            delaySeconds = DELAY_SECONDS_LIST.get(transaction.getRetryTimes() - 1);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delaySeconds);
        return calendar.getTime();
    }

    /**
     * 执行补偿动作时锁定事务（避免并发问题）
     * 注意：集群运行环境请重写此方法，务必加分布式锁保证并发安全。
     *
     * @param transaction 事务实体
     * @return 锁定结果
     */
    protected abstract boolean lockTransactionForCompensate(TccTransaction transaction);

    /**
     * 执行补偿动作后解锁事务
     * 注意：集群运行环境请重写此方法，务必加分布式锁保证并发安全。
     *
     * @param transaction 事务实体
     */
    protected abstract void unlockTransactionForCompensate(TccTransaction transaction);
}
