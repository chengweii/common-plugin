package com.hw.taxi.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chengwei11
 */
public final class CommonResult<T> implements Serializable {
    private int code;
    private String message;
    private T result;
    private Date serverTime;

    private static final int SUCCESS = 200;
    private static final String SUCCESS_MSG = "操作成功";
    public static final int FAIL = 500;

    public CommonResult() {
        serverTime = new Date();
    }

    private CommonResult(int code, String message, T result) {
        this();
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> CommonResult<T> success(String message, T result) {
        return response(SUCCESS, message, result);
    }

    public static <T> CommonResult<T> response(int code, String message, T result) {
        return new CommonResult(code, message, result);
    }

    public static <T> CommonResult<T> success(T result) {
        return new CommonResult(SUCCESS, SUCCESS_MSG, result);
    }

    public static <T> CommonResult<T> fail(String message) {
        return response(FAIL, message, null);
    }

    public static <T> CommonResult<T> fail(int code, String message) {
        return response(code, message, null);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }
}

