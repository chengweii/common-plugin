package com.hw.cache;

import com.hw.cache.provider.cacheaside.RedisCacheAsideCache;

import java.util.Date;

/**
 * 分布式缓存服务
 *
 * @author chengwei11
 * @date 2019/1/23
 */
public interface DistributedCache {
    /**
     * 获取数据
     *
     * @param group     缓存分组（通过分组对强关联的数据缓存进行统一管理）
     * @param cacheKey  缓存KEY
     * @param expire    缓存失效时间
     * @param param     缓存源数据查询依赖参数
     * @param daoAction 缓存源数据查询动作
     * @param <P>       缓存源数据查询依赖参数类型
     * @param <R>       缓存源数据结果类型
     * @return 数据结果
     */
    <P, R> R get(String group, String cacheKey, long expire, P param, RedisCacheAsideCache.DaoAction<P, R> daoAction);

    @FunctionalInterface
    interface DaoAction<P, R> {
        /**
         * 执行缓存源数据查询动作
         *
         * @param param 缓存源数据查询依赖参数
         * @return 缓存源数据结果
         */
        R execute(P param);
    }

    /**
     * 缓存数据包装对象
     *
     * @param <P> 数据获取依赖参数
     * @param <R> 数据结果
     */
    class ResultWrapper<P, R> {
        private P param;
        private R result;
        private String group;
        private long expireTime;
        private String daoActionClzName;

        public ResultWrapper(P param, R result, long expireTime, String daoActionClzName, String group) {
            this.param = param;
            this.result = result;
            this.expireTime = expireTime;
            this.daoActionClzName = daoActionClzName;
            this.group = group;
        }

        public P getParam() {
            return param;
        }

        public R getResult() {
            return result;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public String getDaoActionClzName() {
            return daoActionClzName;
        }

        public String getGroup() {
            return group;
        }
    }
}
