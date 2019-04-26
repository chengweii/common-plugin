package com.hw.external.cache.serialize;

/**
 * 序列化器
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface CacheSerializer {
    String serialize(Object transactionData);

    <T> T deserialize(String transactionData, Class<T> clz);
}
