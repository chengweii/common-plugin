package com.hw.tcc.core.persistence;

import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.serialize.TccSerializer;

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
    boolean begin(TccTransaction transaction);

    /**
     * 获取补偿事务数据
     *
     * @param transactionId 补偿事务业务主键
     * @return 补偿事务数据
     */
    TccTransaction get(String transactionId);

    /**
     * 结束事务
     * 注意：一般结束事务立刻删除事务数据，否则会造成表数据膨胀，影响性能
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean end(TccTransaction transaction);

    /**
     * 失败事务
     * 注意：补偿事务重试指定最大次数后失败（此处可以添加报警逻辑）
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean fail(TccTransaction transaction);

    /**
     * 重试事务
     *
     * @param transaction 事务数据
     * @return 操作结果
     */
    boolean retry(TccTransaction transaction);

    /**
     * 扫描待补偿事务
     *
     * @return 待补偿事务列表
     */
    List<TccTransaction> scan();

    /**
     * 设置持久化服务使用的序列化器
     *
     * @param tccSerializer 序列化器
     */
    void setTccSerializer(TccSerializer tccSerializer);

    /**
     * 获取持久化服务使用的序列化器
     *
     * @return 持久化服务使用的序列化器
     */
    TccSerializer getTccSerializer();

    /**
     * 获取配置信息
     *
     * @return 配置信息
     */
    TccConfig getTccConfig();

    /**
     * 设置配置信息
     *
     * @param tccConfig 配置信息
     */
    void setTccConfig(TccConfig tccConfig);
}
