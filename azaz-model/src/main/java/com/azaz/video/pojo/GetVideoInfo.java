package com.azaz.video.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author c'y'x
 * 上传视频流需要的类
 */
@Data
public class GetVideoInfo {
    /**
     * 视频列表
     */
    private List<VideoDetailInfo> videoList;
    /**
     * 视频总数
     */
    private Integer total;
    /**
     * 最后一个视频的id
     */
    private Integer lastVideoId;
}
