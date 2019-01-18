package com.hw.tcc;

/**
 * 补偿动作
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccCompensateAction {
    boolean execute(String transactionId, String transactionData);
}
