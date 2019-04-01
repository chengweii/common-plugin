package com.hw.promotion.auction.entity.param;

import com.hw.promotion.auction.entity.AuctionInfo;
import lombok.Data;

/**
 * 出价参数
 *
 * @author chengwei11
 * @date 2019/3/29
 */
@Data
public class OfferCheckParam {
    /**
     * 出价参数
     */
    private OfferParam offerParam;
    /**
     * 拍卖信息
     */
    private AuctionInfo auctionInfo;
}
