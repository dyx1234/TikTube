package com.azaz.exception;

import com.azaz.constant.ResponseConstant;
import com.azaz.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 自定义异常捕获类
 * @Author cyx
 */
@ControllerAdvice
@Slf4j
public class ExceptionCatch {

    /**
     * 处理不可控异常
     * @param e Exception
     * @return ResponseResult<Void>
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e){
        log.error("catch exception:{}",e.getMessage());
        return ResponseResult.errorResult(ResponseConstant.SERVER_ERROR);
    }

    /**
     * 处理可控异常  自定义异常
     * @param e CustomException
     * @return ResponseResult<Void>
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult exception(CustomException e){
        log.error("catch exception:{}",e.getMessage());
        return ResponseResult.errorResult(e.getMessage());
    }
}
