package com.hw.promotion.auction;

import com.hw.common.Result;
import com.hw.promotion.auction.entity.param.OfferAfterParam;
import com.hw.promotion.auction.entity.param.OfferCheckParam;

/**
 * 拍卖活动内部实现接口
 *
 * @author chengwei11
 * @date 2019/2/15
 */
public interface AuctionInnerService extends AuctionService {
    /**
     * 出价校验
     *
     * @param offerCheckParam 校验参数
     * @return 校验结果
     */
    Result<Boolean> offerCheck(OfferCheckParam offerCheckParam);

    /**
     * 出价后置处理
     *
     * @param offerAfterParam 后置处理参数
     * @return 处理结果
     */
    Result<Boolean> offerAfterParam(OfferAfterParam offerAfterParam);
}
