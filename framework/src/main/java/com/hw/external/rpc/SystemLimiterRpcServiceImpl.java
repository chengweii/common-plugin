package com.hw.external.rpc;

import com.hw.external.rpc.base.RpcResult;
import com.hw.external.rpc.base.RpcService;
import com.hw.plugins.limiter.SystemLimiter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/28
 */
@RpcService
@Service
public class SystemLimiterRpcServiceImpl implements SystemLimiterRpcService {
    @Resource
    private SystemLimiter systemLimiter;

    @Override
    public RpcResult<Boolean> request(String resourceKey) {
        boolean result = systemLimiter.request(resourceKey);
        return RpcResult.<Boolean>success(result);
    }
}
