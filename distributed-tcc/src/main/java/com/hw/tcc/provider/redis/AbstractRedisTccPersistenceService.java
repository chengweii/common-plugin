package com.hw.tcc.provider.redis;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.hw.tcc.core.persistence.AbstractTccPersistenceService;
import com.hw.tcc.core.persistence.TccTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 基于Redis的分布式补偿事务持久化服务
 *
 * @author chengwei11
 * @date 2019/4/15
 */
public abstract class AbstractRedisTccPersistenceService extends AbstractTccPersistenceService implements RedisClientService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractRedisTccPersistenceService.class);

    private static final String TCC_TRANSACTION_RECORD_KEY = "TCC_TRANSACTION_RECORD_KEY";

    @Override
    public boolean begin(TccTransaction transaction) {
        boolean result = false;
        try {
            boolean saveResult = saveTccTransaction(transaction);
            result = saveResult && this.zAdd(TCC_TRANSACTION_RECORD_KEY, transaction.getNextAt().getTime(), getTransactionRecordKey(transaction.getTransactionId()));
            return result;
        } finally {
            LOGGER.debug("开启事务：transaction={},result={}", transaction, result);
        }
    }

    @Override
    public TccTransaction get(String transactionId) {
        TccTransaction tccTransaction = null;
        try {
            String transactionIdKey = getTransactionRecordKey(transactionId);
            // 根据事务ID列表获取事务信息列表
            Map<String, String> result = mGet(Sets.newHashSet(transactionIdKey));
            if (result == null || result.size() == 0) {
                return null;
            }

            String data = result.get(transactionIdKey);
            tccTransaction = getTccSerializer().deserialize(data, TccTransaction.class);

            return tccTransaction;
        } finally {
            LOGGER.debug("获取待补偿事务：tccTransaction={}", tccTransaction);
        }
    }

    @Override
    public boolean end(TccTransaction transaction) {
        try {
            this.zRem(TCC_TRANSACTION_RECORD_KEY, getTransactionRecordKey(transaction.getTransactionId()));
            this.del(getTransactionRecordKey(transaction.getTransactionId()));
            return true;
        } finally {
            LOGGER.info("分布式补偿事务持久化日志备份结束：transactionId={},transaction={}", transaction.getTransactionId(), transaction);
        }
    }

    @Override
    public boolean fail(TccTransaction transaction) {
        try {
            end(transaction);
            return true;
        } finally {
            LOGGER.error("分布式事务补偿重试失败，请手工处理：transaction={}", transaction);
        }
    }

    @Override
    public boolean retry(TccTransaction transaction) {
        boolean result = false;
        try {
            result = saveTccTransaction(transaction) && zAdd(TCC_TRANSACTION_RECORD_KEY, transaction.getNextAt().getTime(), getTransactionRecordKey(transaction.getTransactionId()));
            return result;
        } finally {
            LOGGER.debug("分布式事务补偿等待重试：transaction={},result={}", transaction, result);
        }
    }

    /**
     * 保存分布式补偿事务数据
     *
     * @param transaction 分布式补偿事务数据
     * @return 是否成功
     */
    private boolean saveTccTransaction(TccTransaction transaction) {
        String transactionData = null;
        long expireTime;
        try {
            expireTime = (getTccConfig().getMaxDelaySeconds() * 1000 + nextLong(1000, 100 * 1000));
            transactionData = this.getTccSerializer().serialize(transaction);
        } finally {
            LOGGER.info("分布式补偿事务持久化日志备份开始：transactionId={},transaction={}", transaction.getTransactionId(), transactionData);
        }

        return this.set(getTransactionRecordKey(transaction.getTransactionId()), transactionData, expireTime, true);
    }

    private static final Random RANDOM = new Random();

    private long nextLong(long startInclusive, long endInclusive) {
        return (long) (startInclusive + ((endInclusive - startInclusive) * RANDOM.nextDouble()));
    }

    /**
     * 获取事务记录缓存KEY
     *
     * @param transactionId 事务ID
     * @return 事务记录缓存KEY
     */
    private String getTransactionRecordKey(String transactionId) {
        return Joiner.on("_").join(TCC_TRANSACTION_RECORD_KEY, transactionId);
    }

    @Override
    public List<TccTransaction> scan() {
        LOGGER.debug("开始扫描待补偿事务，time={}", System.currentTimeMillis());

        // 获取在当前最大事务失效时点过期时间范围内的事务ID列表
        long maxCount = getTccConfig().getMaxCount() > 0 ? getTccConfig().getMaxCount() - 1 : 0;
        Set<String> stringSet = zRangeByScore(TCC_TRANSACTION_RECORD_KEY, 0, System.currentTimeMillis() + this.getTccConfig().getTransactionTimeout(), 0, maxCount);
        if (stringSet == null || stringSet.size() <= 0) {
            return null;
        }

        // 根据事务ID列表获取事务信息列表
        Map<String, String> result = mGet(stringSet);

        if (result == null || result.size() == 0) {
            LOGGER.debug("未扫描到待补偿事务");
            return null;
        }

        List<TccTransaction> tccTransactionList = new ArrayList<TccTransaction>(result.size());
        stringSet.stream().forEach(item -> {
            String data = result.get(item);
            TccTransaction tccTransaction = getTccSerializer().deserialize(data, TccTransaction.class);
            if (TccTransaction.Status.RETRYING.getValue().equals(tccTransaction.getStatus())) {
                // 待重试事务
                tccTransactionList.add(tccTransaction);
            } else if (TccTransaction.Status.EXECUTING.getValue().equals(tccTransaction.getStatus())
                    && System.currentTimeMillis() - tccTransaction.getNextAt().getTime() > this.getTccConfig().getTransactionTimeout()) {
                // 执行超时事务
                tccTransactionList.add(tccTransaction);
            }
        });

        LOGGER.debug("扫描待补偿事务完成：{}", tccTransactionList);

        return tccTransactionList;
    }
}
