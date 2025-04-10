package com.azaz.exception;

/**
 * @author c'y'x
 */
public class QiniuException extends CustomException{
    public QiniuException(){
        super("七牛云上传文件异常");
    }
    public QiniuException(String msg){
        super(msg);
    }

}
