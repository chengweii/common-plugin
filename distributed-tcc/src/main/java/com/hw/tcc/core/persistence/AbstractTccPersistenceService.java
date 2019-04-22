package com.hw.tcc.core.persistence;

import com.hw.tcc.core.config.TccConfig;
import com.hw.tcc.core.serialize.TccSerializer;

/**
 * 分布式补偿事务持久化服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public abstract class AbstractTccPersistenceService implements TccPersistenceService {
    /**
     * 序列化器
     */
    private TccSerializer tccSerializer;
    /**
     * 序列化器
     */
    private TccConfig tccConfig;

    @Override
    public void setTccSerializer(TccSerializer tccSerializer) {
        this.tccSerializer = tccSerializer;
    }

    @Override
    public TccSerializer getTccSerializer() {
        return this.tccSerializer;
    }

    @Override
    public TccConfig getTccConfig() {
        return tccConfig;
    }

    @Override
    public void setTccConfig(TccConfig tccConfig) {
        this.tccConfig = tccConfig;
    }
}
