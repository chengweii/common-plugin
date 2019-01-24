package com.hw.validator.simple;

/**
 * 异常处理
 *
 * @author chengwei11
 * @date 2019/1/24
 */
public class ExceptionHandler {
    public void handControllerException(Throwable t) {
        if (t instanceof SimpleIllegalArgumentException) {
            // TODO 需要统一响应的在Controller拦截器中处理非法参数异常的响应
        }
    }
}
