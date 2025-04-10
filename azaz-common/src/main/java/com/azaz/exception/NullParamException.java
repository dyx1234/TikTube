package com.azaz.exception;

import com.azaz.constant.ResponseConstant;

/**
 * @author shigc
 */
public class NullParamException extends CustomException {

    public NullParamException() {
        super(ResponseConstant.PARAM_REQUIRE);
    }
    public NullParamException(String msg) {
        super(msg);
    }
}
