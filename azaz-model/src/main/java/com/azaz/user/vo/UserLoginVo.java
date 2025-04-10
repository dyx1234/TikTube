package com.azaz.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录VO
 * @author shigc
 */
@Data
@AllArgsConstructor
public class UserLoginVo {
    /**
     * token
     */
    private String token;
    /**
     * 用户id
     */
    private String userId;
}
