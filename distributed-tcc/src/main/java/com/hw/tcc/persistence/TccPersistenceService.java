package com.hw.tcc.persistence;

import java.util.List;

/**
 * 分布式补偿事务持久化服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccPersistenceService {
    /**
     * 开始事务
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean begin(Transaction transaction);

    /**
     * 结束事务
     * 注意：一般结束事务立刻删除事务数据，否则会造成表数据膨胀，影响性能
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean end(Transaction transaction);

    /**
     * 重试事务
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean retry(Transaction transaction);

    /**
     * 扫描待补偿事务
     * 注意：请指定单次扫描事务最大条数，避免内存溢出
     *
     * @param maxCount
     * @return 待补偿事务列表
     */
    List<Transaction> scan(int maxCount);
}
