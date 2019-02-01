package com.hw.tcc.persistence;

import java.util.Date;

/**
 * 事务持久化实体
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class TccTransaction {
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
     * 事务补偿动作序号
     */
    private String actionSerialNo;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCompensateActionClz() {
        return compensateActionClz;
    }

    public void setCompensateActionClz(String compensateActionClz) {
        this.compensateActionClz = compensateActionClz;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(String transactionData) {
        this.transactionData = transactionData;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Date getNextAt() {
        return nextAt;
    }

    public void setNextAt(Date nextAt) {
        this.nextAt = nextAt;
    }

    public Date getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(Date executeAt) {
        this.executeAt = executeAt;
    }

    public String getActionSerialNo() {
        return actionSerialNo;
    }

    public void setActionSerialNo(String actionSerialNo) {
        this.actionSerialNo = actionSerialNo;
    }

    @Override
    public String toString() {
        return "TccTransaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", compensateActionClz='" + compensateActionClz + '\'' +
                ", actionSerialNo='" + actionSerialNo + '\'' +
                ", status=" + status +
                ", transactionData='" + transactionData + '\'' +
                ", retryTimes=" + retryTimes +
                ", nextAt=" + nextAt +
                ", executeAt=" + executeAt +
                '}';
    }

    /**
     * 事务执行状态
     */
    public static enum Status {
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

        Status(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return this.value;
        }
    }
}
