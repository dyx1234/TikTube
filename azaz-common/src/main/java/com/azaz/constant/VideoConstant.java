package com.azaz.constant;

/**
 * @author c'y'x
 */
public class VideoConstant {
    /**
     * 以videoId为key的集合，记录点赞的user集合
     */
    public static final String SET_LIKE_KEY="video_set_like:";

    /**
     * 以videoId为key的集合，记录收藏的user集合
     */
    public static final String SET_COLLECT_KEY="video_set_collect:";

    /**
     * 以userId为key的list，记录点赞过的视频id
     */
    public static final String USER_SET_LIKE_KEY="use_set_like:";

    /**
     * 以userId为key的list，记录收藏过的视频id
     */
    public static final String USER_LIST_COLLECT_KEY="use_set_collect:";

    /**
     * 与videoId对应的video对象
     */
    public static final String VIDEO_ID="video_id:";

    /**
     * 记录user被总点赞数的key
     */
    public static final String USER_LIKES_SUM="user_likes_sum:";

    /**
     * 记录user被总收藏数的key
     */
    public static final String USER_COLLECT_SUM="video_collect_sum:";

    /**
     * 记录userId下的所有视频id,以list存储
     */
    public static final String USER_VIDEO_LIST="user_video_list:";

    /**
     * 点赞数
     */
    public static final String STRING_LIKE_KEY="string_like:";

    /**
     * 收藏数
     */
    public static final String STRING_COLLECT_KEY="string_collect:";

    /**
     * 评论数
     */
    public static final String STRING_COMMENT_KEY="string_comment:";

    /**
     * 点赞类型
     */
    public static final int LIKE_TYPE=1;

    /**
     *  最新一个视频的id
     */
    public static final String LAST_VIDEO_ID = "last_video_id:";
}
