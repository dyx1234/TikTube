package com.azaz.controller;

import com.azaz.interact.dto.MessageListDto;
import com.azaz.interact.dto.MessageSendDto;
import com.azaz.interact.vo.ChatListVo;
import com.azaz.interact.vo.MessageListVo;
import com.azaz.response.ResponseResult;
import com.azaz.service.PrivateMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 私信控制器
 * @author shigc
 */
@RestController
@RequestMapping("/azaz/interact/message")
public class PrivateMessageController {
    @Resource
    private PrivateMessageService privateMessageService;

    /**
     * 发送私信
     * @param messageSendDto 私信发送dto
     * @return ResponseResult
     */
    @PostMapping("/send")
    public ResponseResult sendPrivateMessage(MessageSendDto messageSendDto) {
        return privateMessageService.sendPrivateMessage(messageSendDto);
    }

    /**
     * 私信列表
     * @param messageListDto 私信列表dto
     * @return  ResponseResult
     */
    @GetMapping("/list")
    public ResponseResult<MessageListVo> privateMessageList(MessageListDto messageListDto) {
        return privateMessageService.privateMessageList(messageListDto);
    }

    /**
     * 私信列表
     * @return  ResponseResult
     */
    @GetMapping("/chatList")
    public ResponseResult<ChatListVo> chatList() {
        return privateMessageService.chatList();
    }
}
