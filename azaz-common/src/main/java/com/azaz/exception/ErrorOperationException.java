package com.azaz.exception;

/**
 * 操作异常
 * @author shigc
 */
public class ErrorOperationException extends CustomException {
    public ErrorOperationException(String message) {
        super(message);
    }

    public ErrorOperationException() {
        super("操作异常");
    }

}
