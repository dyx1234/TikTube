package com.azaz.exception;


/**
 * 自定义异常
 * @author shigc
 */
public class CustomException extends RuntimeException {

    public CustomException() {
    }

    public CustomException(String message) {
        super(message);
    }
}
