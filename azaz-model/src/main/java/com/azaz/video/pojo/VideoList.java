package com.azaz.video.pojo;

import lombok.Data;

import java.util.List;

/**
 * 查询收藏列表和发布列表需要的类
 * @author c'y'x
 */
@Data
public class VideoList {
    /**
     * 视频列表
     */
    public List<Video>videoList;

    /**
     * 总数
     */
    public Integer total;

}
