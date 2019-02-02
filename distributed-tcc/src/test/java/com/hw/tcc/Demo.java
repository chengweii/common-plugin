package com.hw.tcc;

import com.hw.tcc.compensate.ActionSerialNoEnum;
import com.hw.tcc.compensate.TccCompensateAction;
import com.hw.tcc.compensate.TccTransactionData;
import com.hw.tcc.provider.TccRetryJob;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 补偿事务测试服务
 *
 * @author chengwei11
 * @since 2019/1/31
 */
@Service
public class Demo implements TccCompensateAction {
    @Resource
    private TccService tccService;

    public static void main(String[] args) throws Throwable {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        TccRetryJob tccRetryJob = appContext.getBean(TccRetryJob.class);
        tccRetryJob.execute();

        Demo demo = appContext.getBean(Demo.class);
        demo.test();
    }

    public void test() throws Throwable {
        Map<String, Object> outerData = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();

        TccService.Result<Map<String, Object>> transactionResult = tccService.execute(ActionSerialNoEnum.ACTION_SERIAL_NO_1, "businessUniqueKey", transactionData, this.getClass(), () -> {
            Map<String, Object> result = businessAction(outerData);
            if (result == null) {
                return TccService.Result.failed(result);
            } else {
                return TccService.Result.success(result);
            }
        });

        Map<String, Object> businessResult = transactionResult.getResult();
        System.out.println(businessResult);

        TccService.Result<Map<String, Object>> transactionResult1 = tccService.execute(ActionSerialNoEnum.ACTION_SERIAL_NO_2, "businessUniqueKey1", transactionData, this.getClass(), () -> {
            Map<String, Object> result = businessAction1(outerData);
            if (result == null) {
                return TccService.Result.failed(result);
            } else {
                return TccService.Result.success(result);
            }
        });

        Map<String, Object> businessResult1 = transactionResult1.getResult();
        System.out.println(businessResult1);
    }

    public Map<String, Object> businessAction(Map<String, Object> data) {
        // 业务操作
        System.out.println("执行业务操作");
        return null;
    }

    public Map<String, Object> businessAction1(Map<String, Object> data) {
        // 业务操作
        System.out.println("执行业务操作1");
        return new HashMap<>();
    }

    @Override
    public boolean execute(TccTransactionData tccTransactionData) {
        ActionSerialNoEnum actionSerialNoEnum = tccTransactionData.getActionSerialNo();

        // 执行补偿操作
        switch (actionSerialNoEnum) {
            case ACTION_SERIAL_NO_1:
                return compensateAction1(tccTransactionData);
            case ACTION_SERIAL_NO_2:
                return compensateAction2(tccTransactionData);
            default:
                return compensateAction1(tccTransactionData);
        }
    }

    private boolean compensateAction1(TccTransactionData tccTransactionData) {
        System.out.println("执行补偿操作");
        return false;
    }

    private boolean compensateAction2(TccTransactionData tccTransactionData) {
        System.out.println("执行补偿操作1");
        return true;
    }
}
