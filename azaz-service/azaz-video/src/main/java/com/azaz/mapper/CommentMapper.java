package com.azaz.mapper;

import com.azaz.video.pojo.Comment;
import com.azaz.video.pojo.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author c'y'x
 */
@Mapper
public interface CommentMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<Comment>{


}
