package com.hw.tcc.persistence;

import java.util.List;

/**
 * 分布式补偿事务持久化服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccPersistenceService {
    boolean begain(Transaction transaction);

    boolean end(Transaction transaction);

    boolean retry(Transaction transaction);

    List<Transaction> scan(int maxCount);
}
