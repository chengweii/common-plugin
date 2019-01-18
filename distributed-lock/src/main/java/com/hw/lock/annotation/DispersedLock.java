package com.hw.lock.annotation;

import com.hw.lock.DistributedLock;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DispersedLock {
    String lockKey();

    int timeout();

    DistributedLock.LockMode lockMode();
}
