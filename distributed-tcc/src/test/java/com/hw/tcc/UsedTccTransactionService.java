package com.hw.tcc;

import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.persistence.TccPersistenceService;
import com.hw.tcc.provider.redis.AbstractRedisTccTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 基于Redis持久化的简单分布式补偿事务服务
 *
 * @author chengwei11
 * @date 2019/4/12
 */
@Service
public class UsedTccTransactionService extends AbstractRedisTccTransactionService {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UsedTccTransactionService.class);
    /**
     * 分布式锁服务
     */
    @Autowired
    private RedisLockService redisLockService;
    @Resource
    private TccPersistenceService tccPersistenceService;
    @Resource
    private TccConfig tccConfig;

    public UsedTccTransactionService(TccPersistenceService tccPersistenceService, TccConfig tccConfig) {
        super(tccPersistenceService, tccConfig);
    }

    @Override
    public void lock(String lockKey, int timeout, LockAction action) {
        RedisLockService.Result result = redisLockService.lock(lockKey, timeout, RedisLockService.LockMode.FAIL_FAST, () -> {
            action.execute();
            return null;
        });

        if (result == null || !result.isSuccess()) {
            LOGGER.info("分布式补偿事务补偿锁定失败:lockKey={},result={}", lockKey, result);
        }
    }
}
