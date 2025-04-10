package com.azaz.interact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信vo
 * @author shigc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageVo {
    /**
     * 发送者id
     */
    private String senderId;

    /**
     * 私信id
     */
    private String messageId;

    /**
    * 私信内容
    */
    private String messageContent;

    /**
     * 消息类型 0－私信 1－ 朋友分享 2－系统消息
     */
    private Integer status;

}
