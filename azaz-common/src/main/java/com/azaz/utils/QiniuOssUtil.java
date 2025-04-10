package com.azaz.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * 七牛云上传工具类
 * @author c'y'x
 */
@Log4j2
@Component
public class QiniuOssUtil {

    static String  accessKey = "dZv53Knxs8XBaPT4jCkyHar7L1u7nJ_LsEM1Fi3T";
    static String secretKey = "8dgc4VO3kFh06GaY_pIlZjJPafJ8GdyOOxCwPXwB";
    static String bucket = "azaz";
    static String CDN="http://s349n62ri.bkt.clouddn.com";

    /**
     * 传入文件字符和已经处理过的文件名称(UUID+文件名称+后缀)
     * @param bytes 文件字符
     * @param objectName 文件名称
     * @return 文件路径
     */
    public static String upload(byte[] bytes,String objectName){

        String filePath="";
        Configuration cfg = new Configuration(Zone.autoZone());
        // 指定分片上传版本
//        cfg. = Configuration.ResumableUploadAPIVersion.V2;
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        //指定key为文件名
        ByteArrayInputStream byteInputStream=new ByteArrayInputStream(bytes);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(byteInputStream, objectName,upToken,null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            filePath= CDN + "/" + putRet.key;
        } catch (QiniuException ex) {
            log.error("{} 七牛云上传失败:{}", objectName, ex.getMessage());
        }
        return filePath;
    }
}
