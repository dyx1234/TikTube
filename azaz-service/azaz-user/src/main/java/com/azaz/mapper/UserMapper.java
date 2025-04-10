package com.azaz.mapper;

import com.azaz.user.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录Mapper
 * @Author shigc
 */
@Mapper
public interface UserMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<User>{
}
