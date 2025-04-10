package com.azaz.exception;

import com.azaz.constant.ResponseConstant;

/**
 * @author shigc
 */
public class UserNotExitedException extends CustomException {

    public UserNotExitedException() {
        super(ResponseConstant.USER_NOT_EXIST);
    }

    public UserNotExitedException(String message) {
        super(message);
    }
}
