package com.hw.taxi.impl;

import com.hw.taxi.DriverService;
import com.hw.taxi.LocationService;
import com.hw.taxi.entity.CommonResult;
import com.hw.taxi.entity.UserLocationInfo;

import javax.annotation.Resource;

/**
 * 司机服务实现
 *
 * @author chengwei11
 * @date 2019/4/2
 */
public class DriverServiceImpl implements DriverService {
    @Resource
    private LocationService locationService;

    public CommonResult<Boolean> reportLocation(UserLocationInfo userLocationInfo) {
        return null;
    }
}
