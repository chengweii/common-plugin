package com.hw.plugins.costtimer;


/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/29
 */
public class CostTimer {
    public long cost(CostAction action) {
        long start = System.currentTimeMillis();
        action.execute();
        return System.currentTimeMillis() - start;
    }

    public interface CostAction {
        void execute();
    }
}
