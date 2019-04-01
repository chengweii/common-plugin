package com.hw.promotion.auction.entity.param;

import com.hw.promotion.auction.entity.OfferRecord;
import lombok.Data;

/**
 * 出价参数
 *
 * @author chengwei11
 * @date 2019/3/29
 */
@Data
public class OfferAfterParam {
    /**
     * 出价信息
     */
    private OfferRecord offerRecord;
}
