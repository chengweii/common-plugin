package com.hw.delayjob.provider.hashwheel;

import com.hw.delayjob.DelayJobService;
import io.netty.util.HashedWheelTimer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/18
 */
public class HashWheelDelayJob implements DelayJobService {
    private HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 10);

    @Override
    public void execute(String jobKey, long delay, TimeUnit timeUnit, final Runnable job) {
        hashedWheelTimer.newTimeout((timeout) -> {
            job.run();
        }, delay, timeUnit);
    }

    public static void main(String[] args) {
        HashWheelDelayJob hashWheelDelayJob = new HashWheelDelayJob();
        hashWheelDelayJob.execute("sdfsd", 3, TimeUnit.SECONDS, () -> {
            System.out.println("任务执行：" + new Date(System.currentTimeMillis()));
            hashWheelDelayJob.execute("sdfsd", 5, TimeUnit.SECONDS, () -> {
                System.out.println("内部任务执行" + new Date(System.currentTimeMillis()));
            });
        });
    }
}
