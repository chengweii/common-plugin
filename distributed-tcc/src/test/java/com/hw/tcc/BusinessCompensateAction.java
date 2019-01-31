package com.hw.tcc;

/**
 * 业务补偿操作
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class BusinessCompensateAction implements TccCompensateAction {
    @Override
    public boolean execute(String transactionId, String transactionData) {
        // 补偿操作
        return false;
    }
}
