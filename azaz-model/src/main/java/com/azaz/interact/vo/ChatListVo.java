package com.azaz.interact.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shigc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatListVo {
    /**
     * 总数
     */
    private Integer total;

    /**
     * 私信列表
     */
    private List<ChatVo> chatList;
}
