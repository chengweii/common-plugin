package com.hw.tcc.provider.database;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hw.tcc.core.BaseTccTransactionService;
import com.hw.tcc.core.compensate.TccCompensateAction;
import com.hw.tcc.core.compensate.TccTransactionData;
import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.persistence.TccPersistenceService;
import com.hw.tcc.core.persistence.TccTransaction;
import com.hw.tcc.core.serialize.TccSerializer;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于数据库持久化的分布式补偿事务服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public abstract class AbstractDbTccTransactionService extends BaseTccTransactionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractDbTccTransactionService.class);

    protected TccPersistenceService tccPersistenceService;
    private List<Integer> delaySecondsList;
    private HashedWheelTimer tccCompensateDelayExecutor;
    private ExecutorService tccCompensateExecutor;
    private ScheduledExecutorService tccCompensateScanExecutor;

    public AbstractDbTccTransactionService(TccSerializer tccSerializer, TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccSerializer, tccConfig);
        this.tccPersistenceService = tccPersistenceService;
        this.init();
    }

    public AbstractDbTccTransactionService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccConfig);
        this.tccPersistenceService = tccPersistenceService;
        this.init();
    }

    private void init() {
        LOGGER.info("分布式补偿事务服务初始化：tccConfig={}", tccConfig);

        this.delaySecondsList = Stream.of(this.tccConfig.getDelaySecondsList().split(",")).map(item -> Integer.valueOf(item)).collect(Collectors.toList());
        this.tccConfig.setMaxDelaySeconds(delaySecondsList.stream().mapToLong(Integer::longValue).sum());
        this.tccConfig.setMaxRetryTimes(delaySecondsList.size());
        this.tccPersistenceService.setTccSerializer(this.tccSerializer);
        this.tccPersistenceService.setTccConfig(this.tccConfig);
        initTransactionCompensateExecutor();
    }

    private void initTransactionCompensateExecutor() {
        LOGGER.info("分布式补偿事务服务执行器（补偿事务扫描器、补偿事务延迟处理器、补偿事务执行器）初始化：tccConfig={}", tccConfig);

        int ticksPerWheel = Double.valueOf(Math.ceil(tccConfig.getTransactionTimeout() / tccConfig.getTickDuration())).intValue();
        this.tccCompensateDelayExecutor = new HashedWheelTimer(new ThreadFactoryBuilder()
                .setNameFormat("tccCompensateDelayExecutor-thread-%d").build(), tccConfig.getTickDuration(), TimeUnit.MILLISECONDS, ticksPerWheel);

        this.tccCompensateExecutor = new ThreadPoolExecutor(1, tccConfig.getMaximumPoolSize(), tccConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(tccConfig.getQueueCapacity()), new ThreadFactoryBuilder()
                .setNameFormat("tccCompensateExecutor-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());

        this.tccCompensateScanExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder()
                .setNameFormat("tccCompensateScanExecutor-thread-%d").setDaemon(true).build());

        this.tccCompensateScanExecutor.scheduleAtFixedRate(() -> {
            this.compensate();
        }, 0, tccConfig.getTransactionTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public <T, R> Result<T> execute(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) {
        ResultWrapper<T> finalResult = new ResultWrapper<T>();

        lock(getTransactionLockKey(transactionId), tccConfig.getLockTimeout(), () -> {
            Result<T> result = executeTransaction(transactionId, transactionData, compensateActionClz, transactionAction);
            finalResult.setResult(result);
        });

        return finalResult.getResult();
    }

    @Override
    public <T, R> Result<T> execute(String transactionId, R transactionData, TransactionAction<T> transactionAction) {
        ResultWrapper<T> finalResult = new ResultWrapper<T>();

        Class compensateActionClz;
        try {
            compensateActionClz = Class.forName(new Exception().getStackTrace()[1].getClassName());
            List<Class> list = Stream.of(compensateActionClz.getInterfaces()).collect(Collectors.toList());
            if (!list.contains(TccCompensateAction.class)) {
                throw new RuntimeException("当前类必须实现事务补偿接口");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("没有找到事务补偿实现类");
        }

        lock(getTransactionLockKey(transactionId), tccConfig.getLockTimeout(), () -> {
            Result<T> result = executeTransaction(transactionId, transactionData, compensateActionClz, transactionAction);
            finalResult.setResult(result);
        });

        return finalResult.getResult();
    }

    /**
     * 获取事务记录锁KEY
     *
     * @param transactionId 事务ID
     * @return 事务记录锁KEY
     */
    private String getTransactionLockKey(String transactionId) {
        return Joiner.on("_").join("TCC_TRANSACTION_LOCK", transactionId);
    }

    /**
     * ResultWrapper
     *
     * @param <T>
     */
    private static class ResultWrapper<T> {
        private Result<T> result;

        public Result<T> getResult() {
            return result;
        }

        public void setResult(Result<T> result) {
            this.result = result;
        }
    }

    private <T, R> Result<T> executeTransaction(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) {
        boolean isRetry = false;
        TccTransaction transaction = new TccTransaction();
        try {
            transaction.setTransactionId(transactionId);
            String serializeData = this.tccSerializer.serialize(transactionData);
            transaction.setTransactionData(serializeData);
            transaction.setCompensateActionClz(compensateActionClz.getName());
            Date now = new Date();
            transaction.setExecuteAt(now);
            transaction.setNextAt(now);
            transaction.setRetryTimes(0);
            transaction.setStatus(TccTransaction.Status.EXECUTING.getValue());
            if (!this.tccPersistenceService.begin(transaction)) {
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
                retryTransaction(transaction);
            } else {
                this.tccPersistenceService.end(transaction);
            }
        }
    }

    /**
     * 重试事务，扫描周期内待执行的事务立刻执行（放入延迟队列中）
     *
     * @param tccTransaction 事务实体
     */
    private void retryTransaction(TccTransaction tccTransaction) {
        this.tccPersistenceService.retry(tccTransaction);
        if (tccTransaction.getNextAt().getTime() - System.currentTimeMillis() <= tccConfig.getTransactionTimeout()) {
            executeTccCompensateAction(tccTransaction);
        }
    }

    /**
     * 执行补偿动作
     *
     * @param tccTransaction 事务实体
     */
    protected void executeTccCompensateAction(TccTransaction tccTransaction) {
        long delayTime = tccTransaction.getNextAt().getTime() - System.currentTimeMillis();
        delayTime = delayTime > 0 ? delayTime : 0;

        this.tccCompensateDelayExecutor.newTimeout((timeout) -> {
            Future future = tccCompensateExecutor.submit(() -> {
                this.lock(getTransactionLockKey(tccTransaction.getTransactionId()), tccConfig.getLockTimeout(), () -> {
                    this.executeCompensateAction(tccTransaction);
                });
            });

            try {
                future.get(tccConfig.getTransactionTimeout(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                if (e instanceof TimeoutException) {
                    future.cancel(true);
                }
                LOGGER.error("分布式补偿事务重试超时：transaction={}", tccTransaction, e);
            }
        }, delayTime, TimeUnit.MILLISECONDS);
    }

    private void executeCompensateAction(TccTransaction tccTransaction) {
        TccTransaction currentTccTransaction = tccPersistenceService.get(tccTransaction.getTransactionId());
        if (currentTccTransaction == null) {
            // 如果事务补偿已执行完毕（结束、失败）则不再执行补偿动作
            return;
        }

        TccCompensateAction tccCompensateAction = this.getTccCompensateAction(tccTransaction.getCompensateActionClz());
        if (tccCompensateAction == null) {
            LOGGER.error("分布式补偿事务重试失败，没有找到对应的补偿动作对象：transaction={}", tccTransaction);
            return;
        }

        boolean isRetry = false;

        try {
            TccTransactionData tccTransactionData = new TccTransactionData();
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

        if (tccTransaction.getRetryTimes() >= this.tccConfig.getMaxRetryTimes()) {
            LOGGER.info("分布式补偿事务重试终止：transactionId={}", tccTransaction.getTransactionId());
            tccTransaction.setStatus(TccTransaction.Status.FAILED.getValue());
            tccPersistenceService.fail(tccTransaction);
            return;
        }

        if (isRetry) {
            Date nextExecuteTime = this.getNextExecuteTime(tccTransaction);
            tccTransaction.setNextAt(nextExecuteTime);
            tccTransaction.setStatus(TccTransaction.Status.RETRYING.getValue());
            tccPersistenceService.retry(tccTransaction);
        } else {
            tccPersistenceService.end(tccTransaction);
        }
    }

    @Override
    protected Date getNextExecuteTime(TccTransaction transaction) {
        Integer delaySeconds = delaySecondsList.get(delaySecondsList.size() - 1);
        if (transaction.getRetryTimes() <= delaySecondsList.size()) {
            delaySeconds = delaySecondsList.get(transaction.getRetryTimes() - 1);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delaySeconds);
        return calendar.getTime();
    }
}

