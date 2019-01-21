package com.hw.tcc.provider.mq;

import com.hw.tcc.TccCompensateAction;
import com.hw.tcc.config.TccConfig;
import com.hw.tcc.persistence.Transaction;
import com.hw.tcc.provider.BaseTccService;
import com.hw.tcc.serialize.TccSerializer;

import java.util.Date;

/**
 * 基于MQ持久化的分布式补偿事务服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class MQTccService extends BaseTccService {
    public MQTccService(TccSerializer tccSerializer, TccConfig tccConfig) {
        super(tccSerializer, tccConfig);
    }

    @Override
    protected Date getNextExecuteTime(Transaction transaction) {
        return null;
    }

    @Override
    public <T, R> Result<T> execute(String transactionId, R transactionData, Class<? extends TccCompensateAction> compensateActionClz, TransactionAction<T> transactionAction) throws Throwable {
        return null;
    }

    @Override
    public void compensate() {
    }
}
