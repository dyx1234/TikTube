package com.azaz.user.dto;

import lombok.Data;

/**
 * @author shigc
 */
@Data
public class UserLoginDto {
    /**
     * 手机号
     */
    public String phone;
    /**
     * 密码
     */
    public String password;
}
