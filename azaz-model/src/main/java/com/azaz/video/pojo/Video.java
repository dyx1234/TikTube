package com.azaz.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author c'y'x
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_video")
public class Video  {
    /**
     * 视频id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频作者id
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 视频作者名
     */
    @TableField("title")
    private String title;

    /**
     * 分区，0为热门，其他待定
     */
    @TableField("section")
    private Integer section;

    /**
     * 视频封面url
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 视频url
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 视频状态。0正常    1删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 点赞数
     */
    @TableField("likes")
    private Long likes;

    /**
     * 收藏数
     */
    @TableField("collects")
    private Long collects;

    /**
     * 评论数
     */
    @TableField("comments")
    private Long comments;

}
