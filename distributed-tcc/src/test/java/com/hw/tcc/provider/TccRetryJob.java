package com.hw.tcc.provider;

import com.hw.tcc.TccService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 补偿事务定时扫描任务实现
 *
 * @author chengwei11
 * @date 2019/1/31
 */
@Service
public class TccRetryJob {
    private ScheduledExecutorService jobExecutor = Executors.newScheduledThreadPool(1);

    @Resource
    private TccService tccService;

    public void execute() {
        jobExecutor.scheduleAtFixedRate(() -> {
            tccService.compensate();
        }, 1, 5, TimeUnit.SECONDS);
    }
}
