package com.hw.lock.annotation;

import com.google.common.base.Strings;
import com.hw.lock.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 分布式锁注解拦截器
 */
@Aspect
public class DistributedLockAspect {
    private DistributedLock distributedLock;
    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public DistributedLockAspect(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Pointcut("@within(com.hw.lock.annotation.DispersedLock) || @annotation(com.hw.lock.annotation.DispersedLock)")
    public void lockPoint() {
    }

    @Around("lockPoint()")
    public Object process(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();

        Signature signature = point.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        DispersedLock dispersedLock = AnnotationUtils.findAnnotation(method, DispersedLock.class);

        String lockKey = generateLockKey(point, method, dispersedLock);

        String secretKey = distributedLock.lock(lockKey, dispersedLock.timeout(), dispersedLock.lockMode());
        if (Strings.isNullOrEmpty(secretKey)) {
            return null;
        }
        try {
            return point.proceed(args);
        } catch (Throwable t) {
            throw t;
        } finally {
            distributedLock.unlock(lockKey, secretKey);
        }
    }

    protected String generateLockKey(ProceedingJoinPoint point, Method method, DispersedLock annotation) {
        Map<String, Object> variables = new HashMap<>(point.getArgs().length);
        if (point.getArgs() != null) {
            String[] pNames = parameterNameDiscoverer.getParameterNames(method);
            for (int i = 0; i < point.getArgs().length; i++) {
                String pName = pNames[i];
                Object arg = point.getArgs()[i];
                variables.put(pName, arg);
            }
        }

        EvaluationContext context = new StandardEvaluationContext();
        variables.forEach((name, value) -> context.setVariable(name, value));
        ExpressionParser parser = new SpelExpressionParser();
        return parser.parseExpression(annotation.lockKey()).getValue(context, String.class);
    }
}
