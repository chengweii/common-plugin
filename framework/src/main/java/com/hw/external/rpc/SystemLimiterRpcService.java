package com.hw.external.rpc;

import com.hw.external.rpc.base.RpcResult;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/28
 */
public interface SystemLimiterRpcService {
    RpcResult<Boolean> request(String resourceKey);
}
