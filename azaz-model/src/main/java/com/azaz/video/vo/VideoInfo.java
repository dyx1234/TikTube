package com.azaz.video.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 视频简略信息
 * @author shigc
 */
@Data
public class VideoInfo {

    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 作者id
     */
    private Long authorId;

    /**
     * 封面url
     */
    private String coverUrl;
    /**
     * 视频url
     */
    private String videoUrl;

    /**
     * 视频标题
     */
    private String title;
}
