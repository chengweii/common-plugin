package com.hw.delayjob.provider.delayqueue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 延迟任务
 *
 * @author chengwei11
 * @date 2019/4/17
 */
public class Task<T extends Runnable> implements Delayed {
    /**
     * 到期时间
     */
    private final long time;

    /**
     * 问题对象
     */
    private final T task;
    private static final AtomicLong atomic = new AtomicLong(0);

    private final long n;

    public Task(long timeout, T t) {
        this.time = System.nanoTime() + timeout;
        this.task = t;
        this.n = atomic.getAndIncrement();
    }

    /**
     * 返回与此对象相关的剩余延迟时间，以给定的时间单位表示
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed another) {
        if (another == this) {
            // compare zero ONLY if same object
            return 0;
        }

        if (another instanceof Task) {
            Task anotherTask = (Task) another;
            long diffTimes = time - anotherTask.time;
            if (diffTimes < 0) {
                return -1;
            } else if (diffTimes > 0) {
                return 1;
            } else if (n < anotherTask.n) {
                return -1;
            } else {
                return 1;
            }
        }

        long d = (getDelay(TimeUnit.NANOSECONDS) - another.getDelay(TimeUnit.NANOSECONDS));

        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    public T getTask() {
        return this.task;
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Task) {
            return object.hashCode() == hashCode() ? true : false;
        }
        return false;
    }
}

