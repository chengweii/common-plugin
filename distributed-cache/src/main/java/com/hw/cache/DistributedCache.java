package com.hw.cache;

/**
 * 分布式缓存服务
 *
 * @author chengwei11
 * @date 2019/1/23
 */
public interface DistributedCache {
    static class ResultWrapper<P, R> {
        private P param;
        private R result;

        public ResultWrapper(P param, R result) {
            this.param = param;
            this.result = result;
        }

        public P getParam() {
            return param;
        }

        public R getResult() {
            return result;
        }
    }
}
