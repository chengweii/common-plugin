package com.hw.promotion.fission;

import com.hw.common.Result;
import com.hw.promotion.fission.model.JoinInfo;
import com.hw.promotion.fission.model.JoinParam;
import com.hw.promotion.fission.model.SupportInfo;
import com.hw.promotion.fission.model.SupportParam;

/**
 * 分享裂变活动服务
 *
 * @author chengwei11
 * @date 2019/2/15
 */
public interface FissionService {
    /**
     * 参与活动
     *
     * @param joinParam 参与参数
     * @return 参与结果
     */
    Result<JoinInfo> join(JoinParam joinParam);

    /**
     * 活动助力
     *
     * @param supportParam 助力信息
     * @return 助力结果
     */
    Result<SupportInfo> support(SupportParam supportParam);
}
