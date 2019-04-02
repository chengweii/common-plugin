package com.hw.taxi;

import com.hw.taxi.entity.CommonResult;
import com.hw.taxi.entity.UserLocationInfo;

/**
 * 基础用户服务
 *
 * @author chengwei11
 * @date 2019/4/2
 */
public interface UserService {
    /**
     * 上报用户位置信息
     *
     * @param userLocationInfo 用户位置信息
     * @return 上报结果
     */
    CommonResult<Boolean> reportLocation(UserLocationInfo userLocationInfo);
}
