package com.hw.tcc.compensate;

/**
 * 事务依赖数据对象
 *
 * @author chengwei11
 * @date 2019/2/1
 */
public class TccTransactionData {
    /**
     * 补偿动作序号
     */
    private ActionSerialNoEnum actionSerialNo;
    /**
     * 事务ID
     */
    private String transactionId;
    /**
     * 序列化的事务依赖数据
     */
    private String serializeData;

    public ActionSerialNoEnum getActionSerialNo() {
        return actionSerialNo;
    }

    public void setActionSerialNo(ActionSerialNoEnum actionSerialNo) {
        this.actionSerialNo = actionSerialNo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSerializeData() {
        return serializeData;
    }

    public void setSerializeData(String serializeData) {
        this.serializeData = serializeData;
    }
}
