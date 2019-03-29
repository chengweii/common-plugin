package com.hw.promotion.auction.entity.param;

import lombok.Data;

/**
 * 出价参数
 *
 * @author chengwei11
 * @date 2019/3/29
 */
@Data
public class OfferParam {
    /**
     * 拍卖编号
     */
    private Long auctionId;
    /**
     * 用户PIN
     */
    private String pin;
    /**
     * 出价金额 单位：分
     */
    private Long price;
}
