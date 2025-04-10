package com.azaz.interact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 私信列表vo
 * @author shigc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageListVo {

    /**
     * 私信总数
     */
    private Integer total;

    /**
     * 最后一条私信id
     */
    private String lastMessageId;

    /**
     * 私信列表
     */
    private List<MessageVo> messages;


}
