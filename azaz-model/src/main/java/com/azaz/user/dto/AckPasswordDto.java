package com.azaz.user.dto;

import lombok.Data;

/**
 * 用户密码传输对象
 * @author shigc
 */
@Data
public class AckPasswordDto {
    /**
     * 用户id
     */
    Long userId;
    /**
     * 用户密码
     */
    String password;
}
