package com.hw.tcc;

import com.hw.tcc.provider.redis.AbstractRedisTccPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 销售系统分布式补偿事务持久化服务
 *
 * @author chengwei11
 * @date 2019/4/15
 */
@Service
public class UsedTccPersistenceService extends AbstractRedisTccPersistenceService {
    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(UsedTccPersistenceService.class);

    private static final String OK = "OK";
    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String PX = "PX";
    private static final String XX = "XX";

    @Override
    public Boolean zAdd(String key, double score, String value) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            Long flag = redisClient.zadd(key, score, value);
            return true;
        } finally {
            redisClient.close();
        }
    }

    @Override
    public Long zRem(String key, String... values) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            return redisClient.zrem(key, values);
        } finally {
            redisClient.close();
        }
    }

    @Override
    public Set<String> zRangeByScore(String key, double min, double max, long offset, long count) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            return redisClient.zrangeByScore(key, min, max, (int) offset, (int) count);
        } finally {
            redisClient.close();
        }
    }

    @Override
    public Boolean set(String key, String value, long expire, boolean exists) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            if (!redisClient.exists(key)) {
                return OK.equals(redisClient.set(key, value, NX, PX, expire));
            }
            return OK.equals(redisClient.set(key, value, exists ? XX : NX, PX, expire));
        } finally {
            redisClient.close();
        }
    }

    @Override
    public Map<String, String> mGet(Set<String> keys) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            if (CollectionUtils.isEmpty(keys)) {
                return null;
            }
            List<String> list = redisClient.mget(keys.toArray(new String[keys.size()]));
            Map<String, String> result = new HashMap<String, String>(keys.size());
            List<String> temp = keys.stream().collect(Collectors.toList());
            keys.stream().forEach(item -> {
                String value = list.get(temp.indexOf(item));
                if (value != null) {
                    result.put(item, value);
                }
            });

            return result;
        } finally {
            redisClient.close();
        }
    }

    @Override
    public Boolean del(String... keys) {
        Jedis redisClient = JedisClient.getJedisClient();
        try {
            Long result = redisClient.del(keys);
            return result != null && result > 0;
        } finally {
            redisClient.close();
        }
    }
}
