package com.azaz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.azaz.clients.UserClient;
import com.azaz.constant.InteractConstant;
import com.azaz.exception.ErrorOperationException;
import com.azaz.exception.ErrorParamException;
import com.azaz.exception.NullParamException;
import com.azaz.interact.dto.MessageListDto;
import com.azaz.interact.dto.MessageSendDto;
import com.azaz.interact.pojo.PrivateMessage;
import com.azaz.interact.vo.ChatListVo;
import com.azaz.interact.vo.ChatVo;
import com.azaz.interact.vo.MessageListVo;
import com.azaz.interact.vo.MessageVo;
import com.azaz.mapper.PrivateMessageMapper;
import com.azaz.response.ResponseResult;
import com.azaz.service.PrivateMessageService;
import com.azaz.service.UserFollowService;
import com.azaz.user.vo.UserPersonalInfoVo;
import com.azaz.utils.ThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 私信service
 * @author shigc
 */
@Service
@Log4j2
public class PrivateMessageServiceImpl implements PrivateMessageService {

    @Resource
    private PrivateMessageMapper privateMessageMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserFollowService userFollowService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private UserClient userClient;


    /**
     * 发送私信
     * @param messageSendDto 私信发送dto
     * @return ResponseResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult sendPrivateMessage(MessageSendDto messageSendDto) {
        log.info("发送私信，参数：{}", messageSendDto);
        //1.1 校验参数
        if (messageSendDto == null || messageSendDto.getReceiverId() == null
                || messageSendDto.getContent() == null || messageSendDto.getStatus() == null) {
            throw new NullParamException();
        }
        //1.2 校验参数
        if (messageSendDto.getContent().length() > InteractConstant.MESSAGE_MAX_LENGTH) {
            throw new ErrorParamException("私信内容不能超过" + InteractConstant.MESSAGE_MAX_LENGTH + "个字符！");
        }
        //1.3 校验参数
        if (messageSendDto.getStatus() != 0 && messageSendDto.getStatus() != 1) {
            throw new ErrorParamException("私信类型错误！");
        }
        Long userId = ThreadLocalUtil.getUserId();
        // 创建锁对象
        RLock redisLock = redissonClient.getLock(InteractConstant.REDIS_LOCK_CHAT_KEY + userId);
        // 尝试获取锁
        boolean isLock = redisLock.tryLock();
        if(!isLock){
            throw new ErrorOperationException("请勿频繁发送消息!");
        }
        try {
            //2.检查是否互关
            ResponseResult<Boolean> ifFollow = userFollowService.ifFollowEachOther(userId, messageSendDto.getReceiverId());
            boolean ifFollowEachOther = ifFollow.getData();
            if (!ifFollowEachOther && messageSendDto.getStatus() == 1) {
                //2.1 未互关，不能分享视频
                throw new ErrorParamException("未互关不能分享视频！");
            }
            // 向redis中添加私信,key为小id-大id
            String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId, messageSendDto.getReceiverId())
                    + "-" + Math.max(userId, messageSendDto.getReceiverId());
            if (!ifFollowEachOther && messageSendDto.getStatus() == 0) {
                //2.3 未互关，检查是否已发送三条私信
                Long count = stringRedisTemplate.opsForList().size(messageKey);
                if (count != null && count >= InteractConstant.MESSAGE_MAX_COUNT) {
                    throw new ErrorParamException("最多向未互关朋友发送三条私信！");
                }
            }
            // 发送私信
            PrivateMessage privateMessage = PrivateMessage.builder()
                    .messageContent(messageSendDto.getContent())
                    .receiverId(messageSendDto.getReceiverId())
                    .senderId(ThreadLocalUtil.getUserId())
                    .createTime(LocalDateTime.now())
                    .status(messageSendDto.getStatus())
                    .build();
            // 向数据库中添加私信，并且填充id
            privateMessageMapper.insert(privateMessage);
            // 异步操作redis
            rocketMQTemplate.convertAndSend("private_message", privateMessage);
        } finally {
            redisLock.unlock();
        }
        return ResponseResult.successResult();
    }

    /**
     * 私信列表
     * @param messageListDto 私信列表dto
     * @return ResponseResult
     */
    @Override
    public ResponseResult<MessageListVo> privateMessageList(MessageListDto messageListDto) {
        log.info("查询私信列表，参数：{}", messageListDto);
        //1. 校验参数
        if (messageListDto == null || messageListDto.getFriendId() == null) {
            throw new NullParamException();
        }
        //2. 从redis中获取私信
        Long userId = ThreadLocalUtil.getUserId();
        Long friendId = messageListDto.getFriendId();
        Long lastMessageId = messageListDto.getLastMessageId();
        if (lastMessageId == null) {
            lastMessageId = 0L;
        }
        //3. 查询redis中是否包含私信key
        String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId, friendId)
                + "-" + Math.max(userId, friendId);
        //3.1 查询redis中是否包含私信key
        Boolean hasKey = stringRedisTemplate.hasKey(messageKey);
        if (hasKey == null || !hasKey || lastMessageId != 0) {
            //3.1.1 redis中没有私信key，从数据库中查询
            return getMessagesInDb(userId, friendId, lastMessageId);
        }
        //4. 从redis中获取私信
        List<String> messageList = stringRedisTemplate.opsForList().range(messageKey, 0, -1);
        if (messageList == null || messageList.isEmpty()) {
            // 4.1 redis中没有私信，从数据库中查询
            return getMessagesInDb(userId, friendId, lastMessageId);
        }
        // 5. 将私信转换为vo
        List<MessageVo> messageVoList = new ArrayList<>();
        for (String message : messageList) {
            MessageVo messageVo = JSON.parseObject(message, MessageVo.class);
            messageVoList.add(messageVo);
        }
        Integer total = messageVoList.size();
        MessageListVo messageListVo = MessageListVo.builder()
                .messages(messageVoList)
                .total(total)
                .lastMessageId(messageVoList.get(messageVoList.size() - 1).getMessageId())
                .build();
        return ResponseResult.successResult(messageListVo);
    }

    /**
     * 私信列表
     * @return ResponseResult
     */
    @Override
    public ResponseResult<ChatListVo> chatList() {

        // 获取redis中的list
        Long userId = ThreadLocalUtil.getUserId();
        String chatListKey = InteractConstant.REDIS_USER_CHAT_LIST + userId;
        Set<String> chatList = stringRedisTemplate.opsForZSet().range(chatListKey, 0, -1);
        List<ChatVo> chatVoList = new ArrayList<>();
        if (chatList == null || chatList.isEmpty()) {
            return ResponseResult.successResult(ChatListVo.builder().chatList(chatVoList).build());
        }
        for (String chatUserId : chatList) {
            Long otherId = Long.parseLong(chatUserId);
            UserPersonalInfoVo userPersonalInfoVo = userClient.getUserPersonalInfo(otherId).getData();
            if (userPersonalInfoVo == null) {
                continue;
            }
            String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId, otherId)
                    + "-" + Math.max(userId, otherId);
            // 获取私信列表的第一条私信
            String message = stringRedisTemplate.opsForList().index(messageKey, 0);
            if (StringUtils.isBlank(message)) {
                continue;
            }
            MessageVo messageVo = JSON.parseObject(message, MessageVo.class);
            ChatVo chatVo = ChatVo.builder()
                    .id(Long.toString(otherId))
                    .username(userPersonalInfoVo.getUsername())
                    .image(userPersonalInfoVo.getImage())
                    .signature(userPersonalInfoVo.getSignature())
                    .latestMessage(messageVo.getMessageContent())
                    .build();
            chatVoList.add(chatVo);
        }
        ChatListVo chatListVo = ChatListVo.builder()
                .chatList(chatVoList)
                .total(chatVoList.size())
                .build();
        return ResponseResult.successResult(chatListVo);
    }


    /**
     * 从数据库中获取私信
     * @param userId 用户id
     * @param friendId 好友id
     * @param lastMessageId 最后一条私信id
     * @return ResponseResult
     */
    private ResponseResult<MessageListVo> getMessagesInDb(Long userId, Long friendId,
                                                          Long lastMessageId) {
        // 1. 从数据库中查询私信
        List<PrivateMessage> privateMessageList = privateMessageMapper.selectByUserIdAndFriendId(userId, friendId, lastMessageId);
        // 2. 将私信转换为vo
        List<MessageVo> messageVoList = new ArrayList<>();
        if (privateMessageList == null || privateMessageList.isEmpty()) {
            return ResponseResult.successResult(MessageListVo.builder().messages(messageVoList).build());
        }
        for (PrivateMessage privateMessage : privateMessageList) {
            // 2.3 封装私信vo
            MessageVo messageVo = MessageVo.builder()
                    .messageId(privateMessage.getId().toString())
                    .messageContent(privateMessage.getMessageContent())
                    .senderId(privateMessage.getSenderId().toString())
                    .status(privateMessage.getStatus())
                    .build();
            messageVoList.add(messageVo);
        }
        // 3. 封装私信列表vo
        Integer total = messageVoList.size();
        MessageListVo messageListVo = MessageListVo.builder()
                .messages(messageVoList)
                .total(total)
                .lastMessageId(messageVoList.get(messageVoList.size() - 1).getMessageId())
                .build();
        return ResponseResult.successResult(messageListVo);
    }

}
