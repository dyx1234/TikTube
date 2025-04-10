package com.azaz.constant;

/**
 * 互动常量类
 * @author shigc
 */
public class InteractConstant {
    /**
     * 私信内容最大长度
     */
    public static final Integer MESSAGE_MAX_LENGTH = 200;

    /**
     * 未互关朋友最多发送私信数量
     */
    public static final Integer MESSAGE_MAX_COUNT = 3;

    /**
     * 私信redis key
     */
    public static final String REDIS_PRIVATE_MESSAGE_KEY = "private_message:";

    /**
     * Redis中保存的最多私信数量
     */
    public static final Integer REDIS_PRIVATE_MESSAGE_MAX_COUNT = 30;

    /**
     * 关注
     */
    public static final Integer FOLLOW_CODE = 1;

    /**
     * 取消关注
     */
    public static final Integer UNFOLLOW_CODE = 0;

    /**
     * 关注总数redis key
     */
    public static final String REDIS_FOLLOW_NUM_KEY = "follow:nums:";

    /**
     * 关注列表redis key
     */
    public static final String REDIS_FOLLOW_KEY = "follow:list:";
    /**
     * 关注锁redis key
     */
    public static final String REDIS_LOCK_FOLLOW_KEY = "lock:follow:";

    /**
     * 私信锁redis key
     */
    public static final String REDIS_LOCK_CHAT_KEY = "lock:follow:";
    /**
     * 互关列表redis key
     */
    public static final String REDIS_FRIEND_KEY = "friend:list:";

    /**
     * 粉丝总数redis key
     */
    public static final String REDIS_FANS_NUM_KEY = "follow:fansNum:";

    /**
     * 聊天列表
     */
    public static final String REDIS_USER_CHAT_LIST = "friend:chatList:";
}
