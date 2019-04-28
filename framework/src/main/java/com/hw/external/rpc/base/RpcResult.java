package com.hw.external.rpc.base;

import lombok.Data;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/28
 */
@Data
public class RpcResult<T> {
    private T data;
    private int code;

    public static <T> RpcResult<T> success(T data) {
        RpcResult<T> rpcResult = new RpcResult<T>();
        rpcResult.data = data;
        rpcResult.code = 200;
        return rpcResult;
    }
}
