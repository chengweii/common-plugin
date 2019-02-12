package com.hw.limiter.annotation;

import com.hw.limiter.FlowLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流资源注解拦截器
 */
@Aspect
public class FlowLimitResourceAspect {
    private FlowLimiter flowLimiter;
    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public FlowLimitResourceAspect(FlowLimiter flowLimiter) {
        this.flowLimiter = flowLimiter;
    }

    @Pointcut("@within(com.hw.limiter.annotation.FlowLimitResource) || @annotation(com.hw.limiter.annotation.FlowLimitResource)")
    public void checkPoint() {
    }

    @Around("checkPoint()")
    public Object process(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();

        Signature signature = point.getSignature();
        Method method = ((MethodSignature) signature).getMethod();

        FlowLimiter.Resource resource = buildResoure(point, method);

        boolean isToLimit = flowLimiter.check(resource);
        if (isToLimit) {
            return null;
        }
        try {
            return point.proceed(args);
        } catch (Throwable t) {
            throw t;
        }
    }

    protected FlowLimiter.Resource buildResoure(ProceedingJoinPoint point, Method method) {
        FlowLimiter.Resource resource = new FlowLimiter.Resource();
        Map<String, Object> variables = new HashMap<String, Object>(point.getArgs().length);
        if (point.getArgs() != null) {
            String[] pNames = parameterNameDiscoverer.getParameterNames(method);
            for (int i = 0; i < point.getArgs().length; i++) {
                String pName = pNames[i];
                Object arg = point.getArgs()[i];
                variables.put(pName, arg);
            }

            resource.setParam(variables);
        }

        String resourceKey = generateResourceKey(point);
        resource.setKey(resourceKey);
        resource.setGroup(flowLimiter.getConfig().getGroup());

        return resource;
    }

    private String generateResourceKey(ProceedingJoinPoint point) {
        Object object = point.getTarget();
        String resourceKey = flowLimiter.getConfig().getGroup() + object.getClass().getSimpleName() + point.getSignature().getName();
        return resourceKey;
    }
}
