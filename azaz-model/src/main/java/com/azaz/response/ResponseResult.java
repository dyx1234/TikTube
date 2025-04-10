package com.azaz.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @author shigc
 */
@Data
public class ResponseResult<T> implements Serializable {

    /**
     * 状态码，2 为成功，其他为失败
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功 不返回数据
     * @return Result
     */
    public static ResponseResult successResult() {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.code = 2;
        return responseResult;
    }

    /**
     * 成功 返回数据
     * @param object 返回数据
     * @return Result
     */
    public static <T> ResponseResult<T> successResult(T object) {
        ResponseResult<T> responseResult = new ResponseResult<T>();
        responseResult.data = object;
        responseResult.code = 2;
        return responseResult;
    }

    /**
     * 失败 返回错误信息,不返回数据
     * @param msg 错误信息
     * @return Result
     */
    public static ResponseResult errorResult(String msg) {
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.message = msg;
        responseResult.code = 1;
        return responseResult;
    }

}
