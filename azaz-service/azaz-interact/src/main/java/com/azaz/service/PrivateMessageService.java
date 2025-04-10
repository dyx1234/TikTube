package com.azaz.service;

import com.azaz.interact.dto.MessageListDto;
import com.azaz.interact.dto.MessageSendDto;
import com.azaz.interact.vo.ChatListVo;
import com.azaz.interact.vo.MessageListVo;
import com.azaz.response.ResponseResult;
import org.springframework.stereotype.Service;

/**
 * 私信service
 * @author shigc
 */
@Service
public interface PrivateMessageService {
    /**
     * 发送私信
     * @param messageSendDto 私信发送dto
     * @return ResponseResult
     */
    ResponseResult sendPrivateMessage(MessageSendDto messageSendDto);

    /**
     * 私信列表
     * @param messageListDto 私信列表dto
     * @return ResponseResult
     */
    ResponseResult<MessageListVo> privateMessageList(MessageListDto messageListDto);

    /**
     * 私信列表
     * @return ResponseResult
     */
    ResponseResult<ChatListVo> chatList();
}
