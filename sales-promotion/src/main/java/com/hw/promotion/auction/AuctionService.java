package com.hw.promotion.auction;

import com.hw.common.Result;
import com.hw.promotion.auction.entity.param.OfferParam;

/**
 * 拍卖活动服务
 *
 * @author chengwei11
 * @date 2019/2/15
 */
public interface AuctionService {
    /**
     * 出价
     *
     * @param offerParam 出价参数
     * @return 出价结果
     * @throws Throwable 异常
     */
    Result<Boolean> offer(OfferParam offerParam) throws Throwable;
}
