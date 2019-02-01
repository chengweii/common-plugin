package com.hw.tcc.provider;

import com.hw.tcc.TccService;
import com.hw.tcc.compensate.TccCompensateAction;
import com.hw.tcc.config.TccConfig;
import com.hw.tcc.persistence.TccTransaction;
import com.hw.tcc.serialize.JsonTccSerializer;
import com.hw.tcc.serialize.TccSerializer;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 分布式补偿事务基础服务
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public abstract class BaseTccService implements TccService {
    protected TccSerializer tccSerializer;
    protected TccConfig tccConfig;

    public BaseTccService(TccSerializer tccSerializer,
                          TccConfig tccConfig) {
        this.tccSerializer = tccSerializer;
        this.tccConfig = tccConfig;
    }

    public BaseTccService(TccConfig tccConfig) {
        this(new JsonTccSerializer(), tccConfig);
    }

    @Resource
    private List<TccCompensateAction> tccCompensateActions;

    /**
     * 获取补偿动作下次执行时间
     *
     * @param transaction 事务实体
     * @return 补偿动作下次执行时间
     */
    protected abstract Date getNextExecuteTime(TccTransaction transaction);

    /**
     * 根据补偿动作类名获取补偿动作对象实例
     *
     * @param compensateActionClz 补偿动作类名
     * @return 补偿动作对象实例
     */
    protected TccCompensateAction getTccCompensateAction(String compensateActionClz) {
        Optional<TccCompensateAction> result = tccCompensateActions.stream().filter(item -> item.getClass().getName().equals(compensateActionClz)).findFirst();

        if (result.isPresent()) {
            return result.get();
        }

        return null;
    }
}
