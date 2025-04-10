package com.azaz.utils;

/**
 * ThreadLocal工具类
 * @author c'y'x
 */
public class ThreadLocalUtil {
    private final static ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户id
     * @param userId 用户id
     */
    public static void setUserId(Long userId){
        USER_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取用户id
     * @return 用户id
     */
    public static Long getUserId(){
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 清除用户id
     */
    public static void clear(){
        USER_THREAD_LOCAL.remove();
    }
}
