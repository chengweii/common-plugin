package com.hw.cache;

/**
 * 缓存服务
 *
 * @author chengwei11
 * @date 2019/3/27
 */
public interface CacheService {
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
    <P, R> R get(String group, String cacheKey, long expire, P param, DaoAction<P, R> daoAction);

    /**
     * 获取数据
     *
     * @param group    缓存分组（通过分组对强关联的数据缓存进行统一管理）
     * @param cacheKey 缓存KEY
     * @param expire   缓存失效时间
     * @param <R>      缓存源数据结果类型
     * @return 数据结果
     */
    <R> R get(String group, String cacheKey, long expire);


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
}
