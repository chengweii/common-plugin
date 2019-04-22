package com.hw.tcc;

import com.hw.tcc.core.compensate.TccCompensateAction;
import com.hw.tcc.core.compensate.TccTransactionData;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 补偿事务测试服务
 *
 * @author chengwei11
 * @since 2019/1/31
 */
@Service
public class Demo implements TccCompensateAction {
    @Resource
    private UsedTccTransactionService tccService;

    public static void main(String[] args) throws Throwable {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        Demo demo = appContext.getBean(Demo.class);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        demo.test();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        executorService.shutdown();

    }

    public void test() throws Throwable {
        Map<String, Object> outerData = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();

        UsedTccTransactionService.Result<Map<String, Object>> transactionResult = tccService.execute("businessUniqueKey", transactionData, () -> {
            Map<String, Object> result = businessAction(outerData);
            if (result == null) {
                return UsedTccTransactionService.Result.failed(result);
            } else {
                return UsedTccTransactionService.Result.success(result);
            }
        });

        if (transactionResult != null) {
            Map<String, Object> businessResult = transactionResult.getResult();
            System.out.println(businessResult);
        }


        UsedTccTransactionService.Result<Map<String, Object>> transactionResult1 = tccService.execute("businessUniqueKey1", transactionData, this.getClass(), () -> {
            Map<String, Object> result = businessAction1(outerData);
            if (result == null) {
                return UsedTccTransactionService.Result.failed(result);
            } else {
                return UsedTccTransactionService.Result.success(result);
            }
        });

        if (transactionResult1 != null) {
            Map<String, Object> businessResult1 = transactionResult1.getResult();
            System.out.println(businessResult1);
        }
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
        System.out.println("执行补偿操作");
        return true;
    }
}
