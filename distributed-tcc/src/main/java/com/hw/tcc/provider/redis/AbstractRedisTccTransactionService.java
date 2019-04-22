package com.hw.tcc.provider.redis;

import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.persistence.TccPersistenceService;
import com.hw.tcc.provider.database.AbstractSimpleDbTccTransactionService;

/**
 * 基于Redis持久化的简单分布式补偿事务服务
 *
 * @author chengwei11
 * @date 2019/4/15
 */
public abstract class AbstractRedisTccTransactionService extends AbstractSimpleDbTccTransactionService {
    public AbstractRedisTccTransactionService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccPersistenceService, tccConfig);
    }
}
