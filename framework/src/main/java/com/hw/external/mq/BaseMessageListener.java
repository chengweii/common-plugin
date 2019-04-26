package com.hw.external.mq;

import com.hw.plugins.limiter.SystemLimiter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述信息
 *
 * @author chengwei11
 * @date 2019/4/26
 */
public abstract class BaseMessageListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseMessageListener.class);

    @Resource
    private SystemLimiter systemLimiter;

    public void onMessage(List<Message> messageList) {
        LOGGER.info("消费消息内容：{}", messageList);

        for (Message message : messageList) {
            if (systemLimiter.request(message.getBusinessId())) {
                LOGGER.info("消费消息触发限流：message={}", message);
                return;
            }
            execute(message);
        }

        LOGGER.info("消费消息完成：{}", messageList);
    }

    protected abstract void execute(Message message);


    @Data
    static class Message {
        private String businessId;
        private String content;
    }
}
