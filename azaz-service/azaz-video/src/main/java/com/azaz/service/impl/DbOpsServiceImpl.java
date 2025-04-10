package com.azaz.service.impl;

import com.azaz.constant.VideoConstant;
import com.azaz.exception.RedissonLockException;
import com.azaz.mapper.VideoMapper;
import com.azaz.service.DbOpsService;
import com.azaz.video.pojo.Video;
import com.azaz.video.pojo.VideoLike;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/**
 * @author c'y'x
 * 此类用作数据库操作，包括redis和mongodb
 */
@Slf4j
@Service
public class DbOpsServiceImpl implements DbOpsService {

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    RedissonClient redissonClient;
    @Resource
    MongoTemplate mongoTemplate;
    @Resource
    VideoMapper videoMapper;


    /**
     * 安全的增加一个int类型的值
     *
     * @param key 视频id
     * @param num 点赞数
     */
    @Override
    public void addIntSafely(String key, int num) {
        //获取分布式锁
        RLock lock = redissonClient.getLock(key+"xyz");
        try {
            //尝试上锁，5s没加上则抛异常
            if(lock.tryLock(5, TimeUnit.SECONDS)){
                int exNum;
                //取出之前的数字
                String s = this.stringRedisTemplate.opsForValue().get(key);
                if(s==null){
                    exNum=0;
                }
                else {
                    exNum=Integer.parseInt(s);
                }
                //加上数字存入
                int now=exNum+num;
                this.stringRedisTemplate.opsForValue().set(key, Integer.toString(now));
            }
        }
        catch (Exception e){
            log.error("redisson加锁失败: {}",e.getMessage());
            throw new RedissonLockException();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * 将数据插入mongoDB中
     * @param userId 用户id
     * @param videoId 视频id
     * @param type 操作类型(1点赞 2收藏 3评论)
     * @param ops 添加到对应字段的参数
     */
    @Override
    @Async
    public void insertIntoMongo(Long userId,Long videoId,int type,Object ops) {
        //查询当用户id和视频id所在的字段
        Query query=Query.query
                (Criteria.where("userId").is(userId).
                        and("videoId").is(videoId));
        VideoLike videoLike = mongoTemplate.findOne(query, VideoLike.class);
        //如果不存在，则添加userId和videoId
        if(videoLike==null){
            videoLike=new VideoLike();
            videoLike.setVideoId(videoId);
            videoLike.setUserId(userId);
        }
        //根据类型对相应字段进行更新操作
        switch (type) {
            //点赞
            case 1 -> videoLike.setIsLike((Integer) ops);

            //收藏
            case 2 -> videoLike.setIsCollect((Integer) ops);

            //评论
            case 3 -> videoLike.setCommentList((ArrayList<String>) ops);

            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        mongoTemplate.save(videoLike);
    }

    /**
     * 把redis中kv类型的数据定时刷新到mysql中
     * 每12小时执行一次
     */
    @PostConstruct
    @Scheduled(cron = "0 */ 720 * * * ?")
    public void fresh(){
        Set<String> likeKeys = stringRedisTemplate.keys(VideoConstant.STRING_LIKE_KEY+'*');
        Set<String> collectKeys = stringRedisTemplate.keys(VideoConstant.STRING_COLLECT_KEY+'*');
        Set<String> commentKeys = stringRedisTemplate.keys(VideoConstant.STRING_COMMENT_KEY+'*');
        if(likeKeys!=null) {
            //更新点赞数
            for (String likeKey : likeKeys) {
                //得到videoId
                String sub= likeKey.substring(VideoConstant.STRING_LIKE_KEY.length());
                Long videoId=Long.parseLong(sub);
                //创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                //获得点赞数
                String likes = stringRedisTemplate.opsForValue().get(likeKey);
                Long likesNum = Long.parseLong(likes == null ? "0" : likes);
                video.setLikes(likesNum);
                //刷新到数据库
                videoMapper.updateById(video);
            }
        }
        if(collectKeys!=null) {
            //更新收藏数
            for (Object collectKey : collectKeys) {
                //得到videoId
                String sub= collectKey.toString().substring(VideoConstant.STRING_COLLECT_KEY.length());
                Long videoId=Long.parseLong(sub);
                //创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                //获得收藏数

                String collects = stringRedisTemplate.opsForValue().get(collectKey);
                Long collectsNum = Long.parseLong(collects == null ? "0" : collects);
                video.setCollects(collectsNum);
                //刷新到数据库
                videoMapper.updateById(video);
            }
        }
        if(commentKeys!=null){
            //更新评论数
            for (Object commentKey : commentKeys) {
                //得到videoId
                String sub= commentKey.toString().substring(VideoConstant.STRING_COMMENT_KEY.length());
                Long videoId=Long.parseLong(sub);
                //创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                //获得评论数
                String comments = stringRedisTemplate.opsForValue().get(commentKey);
                Long commentNum = Long.parseLong(comments == null ? "0" : comments);
                video.setComments(commentNum);
                //刷新到数据库
                videoMapper.updateById(video);
            }
        }
    }

    /**
     * 要是发现redis中like字段过期，则从数据库中查询数据返回，并同时把此视频所有字段刷新到redis
     * @param videoId 视频id
     * @return 点赞数
     */
    @Override
    public Long getSumFromDb(Long videoId){
        //从数据库中查询当前video的数据
        Video video = videoMapper.selectById(videoId);
        //刷新到redis,同时刷新likes,collects,comments字段
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_LIKE_KEY+videoId,video.getLikes().toString());
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_COLLECT_KEY+videoId,video.getCollects().toString());
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_COMMENT_KEY+videoId,video.getComments().toString());
        return video.getLikes();
    }

}
