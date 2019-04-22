package com.hw.tcc.core.persistence;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 事务持久化实体
 *
 * @author chengwei11
 * @date 2019/1/18
 */
@Data
public class TccTransaction implements Serializable {
    /**
     * 事务实体ID
     */
    private Long id;
    /**
     * 补偿事务业务主键
     */
    private String transactionId;
    /**
     * 事务补偿动作类型
     */
    private String compensateActionClz;
    /**
     * 事务状态
     */
    private Byte status;
    /**
     * 事务依赖数据
     */
    private String transactionData;
    /**
     * 已重试次数
     */
    private Integer retryTimes;
    /**
     * 事务下次重试时间
     */
    private Date nextAt;
    /**
     * 事务最近执行时间
     */
    private Date executeAt;

    /**
     * 事务执行状态
     */
    public enum Status {
        /**
         * 待执行或执行中
         */
        EXECUTING((byte) 1),
        /**
         * 待重试或重试中
         */
        RETRYING((byte) 2),
        /**
         * 最终执行失败
         */
        FAILED((byte) 3);
        private byte value;

        Status(Byte value) {
            this.value = value;
        }

        public Byte getValue() {
            return this.value;
        }
    }
}
