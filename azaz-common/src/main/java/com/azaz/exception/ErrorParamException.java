package com.azaz.exception;

import com.azaz.constant.ResponseConstant;

/**
 * @author shigc
 */
public class ErrorParamException extends CustomException{

    public ErrorParamException() {
        super(ResponseConstant.PARAM_INVALID);
    }

    public ErrorParamException(String msg) {
        super(msg);
    }
}
