package com.hw.tcc;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述信息
 *
 * @author chengwei11
 * @since 2019/1/31
 */
public class Demo {
    @Resource
    private TccService tccService;

    public void main() throws Throwable {
        Map<String, Object> outerData = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();

        TccService.Result<Map<String, Object>> transactionResult = tccService.execute("businessUniqueKey", transactionData, BusinessCompensateAction.class, () -> {
            Map<String, Object> result = businessAction(outerData);
            if (result == null) {
                return TccService.Result.failed(result);
            } else {
                return TccService.Result.success(result);
            }
        });

        Map<String, Object> businessResult = transactionResult.getResult();
        System.out.println(businessResult);
    }

    public Map<String, Object> businessAction(Map<String, Object> data) {
        // 业务操作
        return null;
    }
}
