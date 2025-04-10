package com.azaz.mapper;

import com.azaz.video.pojo.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author c'y'x
 */
@Mapper
public interface VideoMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<Video>{

    /**
     * 获取最新的视频
     * @return 视频
     */
    @Select("select * from tb_video order by id desc limit 1")
    Video getLastVideo();
}
