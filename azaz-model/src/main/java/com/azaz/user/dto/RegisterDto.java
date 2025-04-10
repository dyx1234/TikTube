package com.azaz.user.dto;

import lombok.Data;

/**
 * @author shigc
 */
@Data
public class RegisterDto {
    /**
     * 手机号
     */
    public String phone;
    /**
     * 密码
     */
    public String password;

}
