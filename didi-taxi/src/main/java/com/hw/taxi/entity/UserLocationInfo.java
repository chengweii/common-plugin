package com.hw.taxi.entity;

import lombok.Data;

/**
 * 用户位置信息
 *
 * @author chengwei11
 * @date 2019/4/2
 */
@Data
public class UserLocationInfo {
    /**
     * 用户唯一标识
     */
    private String pin;
    /**
     * 用户实时位置坐标（经纬度）
     */
    private String location;
    /**
     * 用户实时位置对应区域ID
     */
    private String areaId;
}
