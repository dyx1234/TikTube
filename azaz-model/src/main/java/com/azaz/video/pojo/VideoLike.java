package com.azaz.video.pojo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author c'y'x
 */
@Data
@Document("video_like")
public class VideoLike implements Serializable {

    /**
     * 主键
     */
    public String id;

    /**
     * 用户id
     */
    public Long userId;

    /**
     * 视频id
     */
    public Long videoId;

    /**
     * 是否点赞
     */
    public Integer isLike;

    /**
     * 是否收藏
     */
    public Integer isCollect;

    /**
     * 是否关注
     */
    public ArrayList<String> commentList;
}
