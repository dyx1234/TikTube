package com.azaz.interact.dto;

import lombok.Data;

/**
 * 私信发送dto
 * @author shigc
 */
@Data
public class MessageListDto {
    /**
     * 好友id
     */
    private Long friendId;

    /**
     * 最后一条(时间最久远的)私信id
     */
    private Long lastMessageId;
}
