package com.hw.tcc.provider;

import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 补偿事务数据持久层实现
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class TccPersistenceServiceImpl implements TccPersistenceService {
    @Override
    public boolean begin(Transaction transaction) {
        return false;
    }

    @Override
    public boolean end(Transaction transaction) {
        return false;
    }

    @Override
    public boolean retry(Transaction transaction) {
        return false;
    }

    @Override
    public List<Transaction> scan(int maxCount) {
        return null;
    }
}
