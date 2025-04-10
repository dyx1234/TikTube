package com.azaz.video.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @author c'y'x
 */
@Data
public class VideoDetailInfo {
    /**
     * id
     */
    private Long id;

    /**
     * 作者id
     */
    private Long authorId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频分类
     */
    private Integer section;

    /**
     * 视频封面
     */
    private String coverUrl;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     *  视频状态0-正常 1-删除
     */
    private Integer status;

    /**
     * 视频点赞数
     */
    private Long likes;

    /**
     * 视频收藏数
     */
    private Long collects;

    /**
     * 视频评论数
     */
    private Long comments;

    /**
     * 视频播放数
     */
    private boolean isLiked;

    /**
     * 视频是否收藏
     */
    private boolean isCollected;

    /**
     * 视频作者
     */
    private String userName;

    /**
     * 视频作者头像
     */
    private String image;

    /**
     * 视频创建时间
     */
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
