package com.hw.tcc.provider;

import com.google.common.collect.Lists;
import com.hw.tcc.persistence.TccPersistenceService;
import com.hw.tcc.persistence.TccTransaction;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 补偿事务数据持久层实现
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class TccPersistenceServiceImpl implements TccPersistenceService {
    private List<TccTransaction> database = Lists.newArrayList();

    @Override
    public boolean begin(TccTransaction transaction) {
        transaction.setId(database.size() + 1L);
        database.add(transaction);

        return true;
    }

    @Override
    public boolean end(TccTransaction transaction) {
        Optional<TccTransaction> result = database.stream().filter(item -> item.getId().equals(transaction.getId())).findFirst();

        if (result.isPresent()) {
            System.out.println(String.format("事务执行完成：%s", result.get().toString()));
            return database.remove(result.get());
        }

        return false;
    }

    @Override
    public boolean fail(TccTransaction transaction) {
        Optional<TccTransaction> result = database.stream().filter(item -> item.getId().equals(transaction.getId())).findFirst();

        if (result.isPresent()) {
            System.out.println(String.format("事务重试失败：%s", result.get().toString()));
            System.out.println(String.format("警报：事务重试失败：%s", result.get().toString()));
            return database.remove(result.get());
        }

        return false;
    }

    @Override
    public boolean retry(TccTransaction transaction) {
        System.out.println(String.format("事务待重试：%s", transaction.toString()));
        return true;
    }

    @Override
    public List<TccTransaction> scan(int maxCount) {
        List<TccTransaction> result = database.stream().filter(item -> {
            return item.getStatus().equals(TccTransaction.Status.RETRYING.getValue()) && item.getNextAt().getTime() <= Calendar.getInstance().getTimeInMillis();
        }).collect(Collectors.toList());

        System.out.println(String.format("事务扫描结果：%s", result.toString()));

        return result;
    }
}
