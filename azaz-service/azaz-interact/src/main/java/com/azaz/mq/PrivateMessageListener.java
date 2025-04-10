package com.azaz.mq;

import com.alibaba.fastjson2.JSON;
import com.azaz.constant.InteractConstant;
import com.azaz.interact.pojo.PrivateMessage;
import com.azaz.interact.vo.MessageVo;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 私信消息监听器
 * @author shigc
 */
@Log4j2
@Component
@RocketMQMessageListener(topic = "private_message", consumerGroup = "private_message_group")
public class PrivateMessageListener implements RocketMQListener<PrivateMessage> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(PrivateMessage privateMessage) {
        log.info("消费者接收到私信消息:{}", privateMessage);
        // 更新chat列表
        Long senderId = privateMessage.getSenderId();
        Long receiverId = privateMessage.getReceiverId();
        // 更新发送者的列表 zset，按照时间排序，越新越靠前
        stringRedisTemplate.opsForZSet().add(InteractConstant.REDIS_USER_CHAT_LIST + senderId, receiverId.toString(), System.currentTimeMillis());
        // 更新接收者的列表
        stringRedisTemplate.opsForZSet().add(InteractConstant.REDIS_USER_CHAT_LIST + receiverId, senderId.toString(), System.currentTimeMillis());
        //将私信转换为vo缓存在redis中
        // 向redis中添加私信,key为小id-大id
        String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(senderId, receiverId)
                + "-" + Math.max(senderId, receiverId);
        //封装私信vo
        MessageVo messageVo = MessageVo.builder()
                .senderId(senderId.toString())
                .messageId(privateMessage.getId().toString())
                .messageContent(privateMessage.getMessageContent())
                .status(privateMessage.getStatus())
                .build();
        // 设置私信过期时间为一周
        stringRedisTemplate.opsForList().leftPush(messageKey, JSON.toJSONString(messageVo));
        stringRedisTemplate.expire(messageKey, 15, java.util.concurrent.TimeUnit.DAYS);
        //检查私信数量是否超过30条，超过则删除最早的一条
        Long size = stringRedisTemplate.opsForList().size(messageKey);
        if (size == null || size > InteractConstant.REDIS_PRIVATE_MESSAGE_MAX_COUNT) {
            stringRedisTemplate.opsForList().rightPop(messageKey);
        }
    }
}
