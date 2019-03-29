package com.hw.promotion.auction.entity;

import lombok.Data;

import java.util.Date;

/**
 * 拍卖信息
 *
 * @author chengwei11
 * @date 2019/3/29
 */
@Data
public class AuctionInfo {
    /**
     * ID
     */
    private Long id;
    /**
     * 拍卖状态
     */
    private Integer status;
    /**
     * 拍卖开始时间
     */
    private Date startTime;
    /**
     * 拍卖结束时间
     */
    private Date endTime;
    /**
     * 当前出价
     */
    private Long currentOfferPrice;
    /**
     * 封顶价
     */
    private Long maxPrice;
    /**
     * 起拍价
     */
    private Long minPrice;
}
