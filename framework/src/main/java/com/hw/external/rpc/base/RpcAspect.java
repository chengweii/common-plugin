package com.hw.external.rpc.base;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Order需要最大，保证RPC切面在降级、限流、报警等切面处理的后面执行。
 */
@Aspect
@Component
@Order(999999)
public class RpcAspect {
    private final static Logger LOGGER = LoggerFactory.getLogger(RpcAspect.class);

    public RpcAspect() {
        LOGGER.info("初始化拦截器成功.");
    }

    @Pointcut("@within(com.hw.external.rpc.base.RpcService) || @annotation(com.hw.external.rpc.base.RpcService)")
    public void RpcPoint() {
    }

    @Around("RpcPoint()")
    public Object process(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        String methodName = getFullMethodName(point);

        LOGGER.info("RpcAspect:method={},args={}", methodName, args);

        Object returnValue = point.proceed(args);

        return returnValue;
    }

    private String getFullMethodName(ProceedingJoinPoint jp) {
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}
