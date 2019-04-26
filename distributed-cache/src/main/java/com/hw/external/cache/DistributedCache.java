package com.hw.external.cache;

import com.hw.external.cache.provider.cacheaside.RedisCacheAsideCache;

/**
 * 分布式缓存服务
 *
 * @author chengwei11
 * @date 2019/1/23
 */
public interface DistributedCache extends CacheService{
    /**
     * 获取数据
     *
     * @param group            缓存分组（通过分组对强关联的数据缓存进行统一管理）
     * @param cacheKey         缓存KEY
     * @param expire           缓存失效时间
     * @param enableLocalCache 是否开启本地缓存
     * @param param            缓存源数据查询依赖参数
     * @param daoAction        缓存源数据查询动作
     * @param <P>              缓存源数据查询依赖参数类型
     * @param <R>              缓存源数据结果类型
     * @return 数据结果
     */
    <P, R> R get(String group, String cacheKey, long expire, boolean enableLocalCache, P param, RedisCacheAsideCache.DaoAction<P, R> daoAction);

    /**
     * 缓存KEY的数据是否有效
     * 实现：无效的缓存KEY直接返回空值结果，且不再缓存空值结果，避免无效KEY恶意攻击造成的缓存溢出甚至宕机情况。
     * 判断KEY是否存在一般通过hbase、es实现bloomFilter。
     *
     * @param group
     * @param cacheKey
     * @return
     */
    boolean isInvalidKey(String group, String cacheKey);

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
