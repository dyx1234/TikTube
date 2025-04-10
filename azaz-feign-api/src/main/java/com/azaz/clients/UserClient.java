package com.azaz.clients;

import com.azaz.interceptor.MyFeignRequestInterceptor;
import com.azaz.response.ResponseResult;
import com.azaz.user.vo.UserPersonalInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务客户端接口
 * @author shigc
 */
@FeignClient(value = "azaz-service-user", configuration = MyFeignRequestInterceptor.class)
public interface UserClient {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult
     */
    @GetMapping("/azaz/user/personal")
    ResponseResult<UserPersonalInfoVo> getUserPersonalInfo(@RequestParam("userId") Long userId);
}
