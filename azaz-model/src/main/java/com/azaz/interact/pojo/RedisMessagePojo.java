package com.azaz.interact.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis消息pojo
 * @author shigc
 */
@Data
public class RedisMessagePojo {
    /**
     * 私信id
     */
    private Long id;

    /**
     * 发送者id
     */
    private Long senderId;

    /**
     * 接收者id
     */
    private Long receiverId;
    /**
     * 私信类型，0-默认 1-朋友分享的视频 2-系统消息
     */
    private Integer messageType;

    /**
     * 私信内容
     */
    private String messageContent;

    /**
     * 私信状态，0-正常 1-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
