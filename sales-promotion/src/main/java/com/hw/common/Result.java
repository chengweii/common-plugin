package com.hw.common;

import lombok.Data;

/**
 * 响应结果
 *
 * @author chengwei11
 * @date 2019/2/15
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result success(T data) {
        return new Result<T>(200, "", data);
    }

    public static <T> Result success(String message, T data) {
        return new Result<T>(200, message, data);
    }

    public static <T> Result failed(T data) {
        return new Result<T>(0, "", data);
    }

    public static <T> Result failed(int code, String message, T data) {
        return new Result<T>(code, message, data);
    }

    public static <T> Result common(int code, String message, T data) {
        return new Result<T>(code, message, data);
    }

    public boolean isSuccess() {
        return 200 == code;
    }
}
