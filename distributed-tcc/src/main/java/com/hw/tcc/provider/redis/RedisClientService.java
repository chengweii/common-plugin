package com.hw.tcc.provider.redis;

import java.util.Map;
import java.util.Set;

/**
 * Redis 客户端服务
 *
 * @author chengwei11
 * @date 2019/4/15
 */
public interface RedisClientService {
    /**
     * zAdd
     *
     * @param key   key
     * @param score score
     * @param value value
     * @return Boolean
     */
    Boolean zAdd(String key, final double score, String value);

    /**
     * zRem
     *
     * @param key    key
     * @param values values
     * @return Long
     */
    Long zRem(String key, String... values);


    /**
     * zRangeByScore
     *
     * @param key    key
     * @param min    min
     * @param max    max
     * @param offset offset
     * @param count  count
     * @return Set<String>
     */
    Set<String> zRangeByScore(String key, double min, double max, long offset, long count);

    /**
     * set
     *
     * @param key    key
     * @param value  value
     * @param expire expire
     * @param exists exists
     * @return Boolean
     */
    Boolean set(String key, String value, long expire, boolean exists);

    /**
     * mGet
     *
     * @param keys keys
     * @return Map<String                               ,                               String>
     */
    Map<String, String> mGet(Set<String> keys);

    /**
     * del
     *
     * @param keys keys
     * @return Boolean
     */
    Boolean del(String... keys);
}
