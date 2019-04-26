package com.hw.external.cache;

/**
 * 旁路缓存型缓存
 * 实现：缓存中获取不到数据则从持久层（数据库/ES）获取。
 *
 * @author chengwei11
 * @date 2019/1/23
 */
public interface CacheAsideCache extends DistributedCache {
}
