package com.azaz.service;

import org.springframework.stereotype.Service;

/**
 * @author c'y'x
 * 数据库操作接口
 */
@Service
public interface DbOpsService {
    /**
     * 安全的增加一个int类型的值
     *
     * @param key 视频id
     * @param num 点赞数
     */
    void addIntSafely(String key, int num);

    /**
     *  向mongo中插入一条数据
     * @param userId 用户id
     * @param videoId 视频id
     * @param type 类型
     * @param ops 操作
     */
    void insertIntoMongo(Long userId,Long videoId,int type,Object ops);

    /**
     * 从mongo中获取数据
     * @param videoId 视频id
     * @return 点赞数
     */
    Long getSumFromDb(Long videoId);
}
