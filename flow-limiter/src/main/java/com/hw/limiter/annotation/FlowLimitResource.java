package com.hw.limiter.annotation;

import java.lang.annotation.*;

/**
 * 限流资源
 *
 * @author chengwei11
 * @date 2019/2/12
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowLimitResource {
}
