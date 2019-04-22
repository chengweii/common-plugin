package com.hw.tcc.core.compensate;

import lombok.Data;

import java.io.Serializable;

/**
 * 事务依赖数据对象
 *
 * @author chengwei11
 * @date 2019/2/1
 */
@Data
public class TccTransactionData implements Serializable {
    /**
     * 事务ID
     */
    private String transactionId;
    /**
     * 序列化的事务依赖数据
     */
    private String serializeData;
}
