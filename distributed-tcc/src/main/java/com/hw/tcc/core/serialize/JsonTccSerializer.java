package com.hw.tcc.core.serialize;

import com.alibaba.fastjson.JSON;

/**
 * Json序列化器
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class JsonTccSerializer implements TccSerializer {
    @Override
    public String serialize(Object transactionData) {
        return JSON.toJSONString(transactionData);
    }

    @Override
    public <T> T deserialize(String transactionData, Class<T> clz) {
        return JSON.parseObject(transactionData, clz);
    }
}
