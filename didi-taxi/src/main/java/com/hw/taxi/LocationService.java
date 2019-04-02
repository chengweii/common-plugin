package com.hw.taxi;

import com.hw.taxi.entity.UserLocationInfo;

/**
 * 位置服务
 *
 * @author chengwei11
 * @date 2019/4/2
 */
public interface LocationService {
    /**
     * 计算区域ID
     *
     * @param userLocationInfo 用户坐标信息
     * @return 区域ID
     */
    Long calculateAreaId(UserLocationInfo userLocationInfo);
}
