package com.hw.lock;

import com.hw.lock.DistributedLock;
import com.hw.lock.annotation.DispersedLock;

import javax.annotation.Resource;

/**
 * @author: chengwei11
 * @since: 2018/12/7 13:49
 * @description:
 */
public class Demo {
    @Resource
    private DistributedLock distributedLock;

    public void main() throws Throwable {
        String outerData = "外部依赖数据";

        // 通过拉姆达表达式
        DistributedLock.Result<String> result = distributedLock.<String>lock("2018110021002", 5000, DistributedLock.LockMode.TRY_LOCK, () -> {
            System.out.println(outerData);
            return DistributedLock.Result.success(1021, "通过拉姆达表达式使用", outerData);
        });

        System.out.println(String.format("锁定结果：%s",result.isSuccess()));

        // 通过匿名内部类
        DistributedLock.Result<String> result1 = distributedLock.lock("2018110021002", 5000, DistributedLock.LockMode.TRY_LOCK, new DistributedLock.LockAction<String>() {
            @Override
            public DistributedLock.Result<String> execute() {
                System.out.println(outerData);
                return DistributedLock.Result.success(1031, "通过匿名内部类使用", new String());
            }
        });

        System.out.println(String.format("锁定结果：%s",result1.isSuccess()));
    }

    /**
     * 通过注解
     *
     * @param businessKey 业务主键
     * @param otherData   其他业务数据
     */
    @DispersedLock(lockKey = "#businessKey", timeout = 5000, lockMode = DistributedLock.LockMode.TRY_LOCK)
    public void businessAction(String businessKey, Object otherData) {

    }
}
