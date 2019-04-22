package com.hw.tcc.core.serialize;

/**
 * 序列化器
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccSerializer {
    String serialize(Object transactionData);

    <T> T deserialize(String transactionData, Class<T> clz);
}
