package com.azaz.interact.dto;

import lombok.Data;

/**
 * 分享视频dto
 */
@Data
public class ShareVideoDto {
    /**
     * 被分享的视频id
     */
    private Long videoId;

    /**
     * 接收者id
     */
    private Long receiverId;

}
