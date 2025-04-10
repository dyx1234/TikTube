package com.azaz.constant;

/**
 * 用户信息常量类
 * @author shigc
 */
public class UserConstant {

    /**
     * 用户名最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;
    /**
     * 用户名最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";

    /**
     * 用户简略信息的redis前缀
     */
    public static final String REDIS_USER_INFO = "user:info:";

    /**
     * 用户简略信息的redis过期时间
     */
    public static final Integer REDIS_USER_INFO_TTL = 24 * 60 * 60;
    /**
     * 默认用户名前缀
     */
    public static final String DEFAULT_USER_NAME_PRE = "azaz_";

    /**
     * 默认用户个性签名
     */
    public static final String DEFAULT_USER_SIGNATURE = "这个人很懒，什么都没有留下";

    /**
     * 用户登录token的redis前缀
     */
    public static final String REDIS_LOGIN_TOKEN = "user:login:token:";

    /**
     * 用户登录token的redis过期时间
     */
    public static final long LOGIN_USER_TTL = 2 * 60 * 60L;

    /**
     * 用户注册锁的redis前缀
     */
    public static final String USER_REGISTER_LOCK = "user:register:lock";
}
