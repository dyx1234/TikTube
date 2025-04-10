package com.azaz.mapper;

import com.azaz.interact.pojo.Follow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注mapper接口类
 * @author shigc
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}
