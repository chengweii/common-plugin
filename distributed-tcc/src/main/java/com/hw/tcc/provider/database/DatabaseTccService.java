package com.hw.tcc.provider.database;

import com.google.common.collect.Lists;
import com.hw.tcc.TccCompensateAction;
import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.Transaction;
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

    public DatabaseTccService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService) {
        super(tccSerializer);
        this.tccPersistenceService = tccPersistenceService;
    }

    @Override
    public <T, R> Result<T> execute(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) throws Throwable {
        boolean isRetry = false;
        Transaction transaction = new Transaction();
        try {
            transaction.setTransactionId(transactionId);
            String serializeData = tccSerializer.serialize(transactionData);
            transaction.setTransactionData(serializeData);
            transaction.setCompensateActionClz(compensateActionClz.getName());
            Date now = new Date();
            transaction.setExecuteAt(now);
            transaction.setNextAt(now);
            transaction.setRetryTimes(0);
            transaction.setStatus(Transaction.Status.EXECUTING.getValue());
            if (!tccPersistenceService.begain(transaction)) {
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
                tccPersistenceService.retry(transaction);
            } else {
                tccPersistenceService.end(transaction);
            }
        }
    }

    /**
     * 执行补偿动作
     *
     * @param transaction 事务实体
     */
    protected void executeTccCompensateAction(Transaction transaction) {
        TccCompensateAction tccCompensateAction = getTccCompensateAction(transaction.getCompensateActionClz());
        if (tccCompensateAction == null) {
            LOGGER.error("分布式补偿事务重试失败，没有找到对应的补偿动作对象：transaction={}", transaction);
            return;
        }

        boolean isRetry = false;

        if (!lockTransactionForCompensate(transaction)) {
            LOGGER.error("分布式补偿事务重试失败，当前事务已被锁定：transaction={}", transaction);
            return;
        }

        try {
            try {
                if (tccCompensateAction.execute(transaction.getTransactionId(), transaction.getTransactionData())) {
                    LOGGER.info("分布式补偿事务重试成功：transactionId={}", transaction.getTransactionId());
                } else {
                    isRetry = true;
                    LOGGER.error("分布式补偿事务重试失败：transactionId={}", transaction.getTransactionId());
                }
            } catch (Throwable t) {
                isRetry = true;
                LOGGER.error("分布式补偿事务重试失败：transaction={}", transaction, t);
            }

            transaction.setRetryTimes(transaction.getRetryTimes() + 1);

            if (isRetry) {
                Date nextExecuteTime = getNextExecuteTime(transaction);
                transaction.setNextAt(nextExecuteTime);
                tccPersistenceService.retry(transaction);
            } else {
                tccPersistenceService.end(transaction);
            }
        } finally {
            unlockTransactionForCompensate(transaction);
        }
    }

    /**
     * 延迟重试时间
     */
    private static final List<Integer> DELAY_SECONDS_LIST = Lists.newArrayList(1, 2, 3, 1 * 60, 2 * 60, 4 * 60, 16 * 60, 256 * 60);

    @Override
    protected Date getNextExecuteTime(Transaction transaction) {
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
     *
     * @param transaction 事务实体
     * @return 锁定结果
     */
    protected abstract boolean lockTransactionForCompensate(Transaction transaction);

    /**
     * 执行补偿动作后解锁事务
     *
     * @param transaction 事务实体
     */
    protected abstract void unlockTransactionForCompensate(Transaction transaction);
}
