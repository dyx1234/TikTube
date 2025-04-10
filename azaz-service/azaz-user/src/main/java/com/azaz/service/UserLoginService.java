package com.azaz.service;

import com.azaz.response.ResponseResult;
import com.azaz.user.dto.RegisterDto;
import com.azaz.user.dto.UserLoginDto;
import com.azaz.user.vo.UserLoginVo;
import org.springframework.stereotype.Service;

/**
 * 用户登录服务接口
 * @author shigc
 */
@Service
public interface UserLoginService {
    /**
     * 用户注册
     * @param registerDto 注册信息
     * @return 注册结果
     */
    ResponseResult register(RegisterDto registerDto);

    /**
     * 用户登录
     * @param userLoginDto 登录信息
     * @return 登录结果
     */
    ResponseResult<UserLoginVo> userLogin(UserLoginDto userLoginDto);
}
