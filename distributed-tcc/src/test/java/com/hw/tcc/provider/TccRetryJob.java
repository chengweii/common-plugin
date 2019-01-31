package com.hw.tcc.provider;

import com.hw.tcc.TccService;

import javax.annotation.Resource;

/**
 * 补偿事务定时扫描任务实现
 *
 * @author chengwei11
 * @date 2019/1/31
 */
public class TccRetryJob {
    @Resource
    private TccService tccService;

    public void execute(){
        tccService.compensate();
    }
}
