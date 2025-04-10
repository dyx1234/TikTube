package com.azaz.exception;

import com.azaz.constant.ResponseConstant;

/**
 * @author shigc
 */
public class PasswordErrorException extends CustomException{

        public PasswordErrorException() {
            super(ResponseConstant.PASSWORD_ERROR);
        }

        public PasswordErrorException(String msg) {
            super(msg);
        }
}
