package com.hw.tcc.compensate;

/**
 * 补偿动作
 * 注意：由于补偿采用的是重试策略，所以补偿动作实现请务必支持幂等操作，否则可能会造成重复操作情况。
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public interface TccCompensateAction {
    /**
     * 执行补偿动作
     *
     * @param tccTransactionData 事务依赖数据对象
     * @return 是否成功（补偿失败会执行重试）
     */
    boolean execute(TccTransactionData tccTransactionData);
}
