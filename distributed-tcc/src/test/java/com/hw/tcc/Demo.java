package com.hw.tcc;

import com.alibaba.fastjson.JSON;
import com.hw.tcc.core.compensate.TccCompensateAction;
import com.hw.tcc.core.compensate.TccTransactionData;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        for (int i = 0; i < 1; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Thread.sleep((new Random()).nextInt(60 * 1000));
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
        String key = UUID.randomUUID().toString();
        Map<String, Object> outerData = new HashMap<String, Object>();
        outerData.put("transactionId", key);
        Map<String, Object> transactionData = new HashMap<String, Object>();
        transactionData.put("transactionId", key);

        UsedTccTransactionService.Result<Map<String, Object>> transactionResult = tccService.execute(key, transactionData, () -> {
            Map<String, Object> result = businessAction(outerData);
            if (result == null) {
                return UsedTccTransactionService.Result.failed(result);
            } else {
                return UsedTccTransactionService.Result.success(result);
            }
        });

        if (transactionResult != null) {
            Map<String, Object> businessResult = transactionResult.getResult();
            // System.out.println(businessResult);
        }
    }

    public Map<String, Object> businessAction(Map<String, Object> data) {
        // 业务操作
        System.out.println(String.format("正在执行业务操作:%s", data));
        return 2 % 2 == 0 ? null : new HashMap<>();
    }

    public Map<String, Object> businessAction1(Map<String, Object> data) {
        // 业务操作
        System.out.println("正在执行业务操作1");
        return new HashMap<>();
    }

    @Override
    public boolean execute(TccTransactionData tccTransactionData) {
        System.out.println(String.format("正在执行补偿操作:%s", JSON.toJSON(tccTransactionData)));
        return false;
    }
}
