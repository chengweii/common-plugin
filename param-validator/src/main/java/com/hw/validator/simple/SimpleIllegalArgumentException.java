package com.hw.validator.simple;

/**
 * 简单非法参数异常
 *
 * @author chengwei11
 * @date 2019/1/24
 */
public class SimpleIllegalArgumentException extends IllegalArgumentException {
    public SimpleIllegalArgumentException(String s) {
        super(s);
    }
}
