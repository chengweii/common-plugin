package com.hw.tcc;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Jedis客户端
 *
 * @author chengwei11
 * @date 2019/4/22
 */

public class JedisClient {
    /**
     * Jedis客户端
     */
    private static JedisPool pool = new JedisPool("127.0.0.1", 6379);

    public static Jedis getJedisClient() {
        return pool.getResource();
    }
}
