package com.hw.limiter;

/**
 * 限流器
 *
 * @author chengwei11
 * @date 2019/2/12
 */
public interface FlowLimiter {

    /**
     * 检查资源访问是否被限制
     *
     * @param limitResource 资源信息
     * @return 是否被限制
     */
    boolean check(LimitResource limitResource);

    /**
     * 获取限流器基本配置信息
     *
     * @return 配置信息
     */
    LimitConfig getLimitConfig();

    class LimitConfig {
        /**
         * 资源分组
         */
        private String group;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    class LimitResource {
        /**
         * 资源分组
         */
        private String group;
        /**
         * 资源唯一标识
         */
        private String key;
        /**
         * 资源访问依赖参数
         */
        private Object param;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getParam() {
            return param;
        }

        public void setParam(Object param) {
            this.param = param;
        }
    }
}
