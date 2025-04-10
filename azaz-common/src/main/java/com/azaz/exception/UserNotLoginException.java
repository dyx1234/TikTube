package com.azaz.exception;

import com.azaz.constant.ResponseConstant;

/**
 * 用户未登录异常
 * @author shigc
 */
public class UserNotLoginException extends CustomException {
    public UserNotLoginException() {
        super(ResponseConstant.NEED_LOGIN);
    }
}
