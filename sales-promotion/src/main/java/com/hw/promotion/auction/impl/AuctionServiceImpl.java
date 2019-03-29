package com.hw.promotion.auction.impl;

import com.google.common.base.Joiner;
import com.hw.common.Result;
import com.hw.lock.DistributedLock;
import com.hw.promotion.auction.AuctionService;
import com.hw.promotion.auction.dao.OfferRecordDao;
import com.hw.promotion.auction.entity.AuctionInfo;
import com.hw.promotion.auction.entity.OfferRecord;
import com.hw.promotion.auction.entity.param.OfferParam;
import com.hw.util.JsonUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;

/**
 * 拍卖活动服务
 *
 * @author chengwei11
 * @date 2019/2/15
 */
public class AuctionServiceImpl implements AuctionService {
    @Resource
    private DistributedLock distributedLock;
    @Resource
    private OfferRecordDao offerRecordDao;
    @Resource
    private Jedis jedis;

    private static final String AUCTION_CURRENT_INFO_CACHE_KEY_PREFIX = "AUCTION_CURRENT_INFO";
    private static final String OFFER_LOCK_KEY_PREFIX = "OFFER_LOCK";
    private static final int OFFER_LOCK_TIMEOUT = 30 * 1000;

    @Override
    public Result<Boolean> offer(OfferParam offerParam) throws Throwable {
        // 首先获取当前拍卖信息
        AuctionInfo auctionInfo = getCurrentAuctionInfo(offerParam.getAuctionId());

        // 然后校验活动有效性、参与资格
        Result<Boolean> checkResult = check(auctionInfo, offerParam);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        String offerLockKey = Joiner.on("_").join(OFFER_LOCK_KEY_PREFIX, offerParam.getAuctionId());
        // 出价操作直接上锁
        DistributedLock.Result<Boolean> result=distributedLock.lock(offerLockKey, OFFER_LOCK_TIMEOUT, DistributedLock.LockMode.FAIL_OVER, () -> {
            // 双重校验出价有效性
            if (offerParam.getPrice() < auctionInfo.getCurrentOfferPrice()) {
                return DistributedLock.Result.success(1004, "出价必须超过最高价", false);
            }

            OfferRecord offerRecord = new OfferRecord();

            boolean flag = offerRecordDao.insert(offerRecord);

            // 出价成功，刷新最新出价信息到当前拍卖信息缓存
            if(flag){
                refreshAuctionCurrentInfo(offerRecord);
            }

            return DistributedLock.Result.success(true);
        });

        return Result.common(result.getCode(),result.getMessage(),result.getResult());
    }

    private void refreshAuctionCurrentInfo(OfferRecord offerRecord){
        // TODO 公平性保证的关键策略：最后5秒钟前台查询最新出价信息时仅显示5秒前的最新出价，防止刷子用户最后一秒出价
        // 刷新最新出价信息到当前拍卖信息缓存
    }

    private AuctionInfo getCurrentAuctionInfo(Long auctionId) {
        String auctionCurrentInfoCacheKey = Joiner.on("_").join(AUCTION_CURRENT_INFO_CACHE_KEY_PREFIX, auctionId);
        String auctionInfoJson = jedis.get(auctionCurrentInfoCacheKey);
        AuctionInfo auctionInfo = JsonUtils.fromJson(auctionInfoJson, AuctionInfo.class);
        return auctionInfo;
    }

    private Result<Boolean> check(AuctionInfo auctionInfo, OfferParam offerParam) {
        // 拍卖活动状态、时间检查
        if (System.currentTimeMillis() < auctionInfo.getStartTime().getTime() || System.currentTimeMillis() > auctionInfo.getEndTime().getTime()) {
            return Result.failed(1001, "拍卖活动未开始或已结束", false);
        }

        // 出价资格校验（风控）
        Result<Boolean> riskResult = risk(auctionInfo, offerParam);
        if (!riskResult.isSuccess()) {
            return Result.failed(1000, "活动太火爆，请稍候再试", false);
        }

        // 出价有效性校验（是否高于起拍价、低于封顶价、超过最高价）
        if (offerParam.getPrice() < auctionInfo.getMinPrice()) {
            return Result.failed(1002, "出价必须高于起拍价", false);
        } else if (offerParam.getPrice() > auctionInfo.getMaxPrice()) {
            return Result.failed(1003, "出价必须低于封顶价", false);
        } else if (offerParam.getPrice() < auctionInfo.getCurrentOfferPrice()) {
            return Result.failed(1004, "出价必须超过最高价", false);
        }

        return Result.failed(false);
    }

    private Result<Boolean> risk(AuctionInfo auctionInfo, OfferParam offerParam) {
        return Result.failed(false);
    }
}
