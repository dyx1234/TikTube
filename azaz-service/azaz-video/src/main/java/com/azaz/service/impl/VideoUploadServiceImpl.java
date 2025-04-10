package com.azaz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.azaz.clients.UserClient;
import com.azaz.constant.VideoConstant;
import com.azaz.exception.*;
import com.azaz.mapper.VideoMapper;
import com.azaz.response.ResponseResult;
import com.azaz.service.DbOpsService;
import com.azaz.service.VideoUploadService;
import com.azaz.user.vo.UserPersonalInfoVo;
import com.azaz.utils.QiniuOssUtil;
import com.azaz.utils.ThreadLocalUtil;
import com.azaz.video.dto.VideoPublishDto;
import com.azaz.video.pojo.GetVideoInfo;
import com.azaz.video.pojo.Video;
import com.azaz.video.pojo.VideoDetailInfo;
import com.azaz.video.pojo.VideoLike;
import com.azaz.video.vo.VideoDetail;
import com.azaz.video.vo.VideoInfo;
import com.azaz.video.vo.VideoUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author c'y'x
 */
@Service
@Slf4j
public class VideoUploadServiceImpl implements VideoUploadService {
    @Resource
    VideoMapper videoMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    DbOpsService dbOpsService;
    @Resource
    UserClient userClient;
    @Resource
    MongoTemplate mongoTemplate;
    @Resource
    RocketMQTemplate rocketMQTemplate;

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;

    /**
     * 线程池
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    /**
     * 发布视频
     * @param videoPublishDto 视频信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseResult<VideoUploadVo> publish(VideoPublishDto videoPublishDto){
        //得到userId
        Long userId = ThreadLocalUtil.getUserId();
        //参数校验
        if(userId == null){
            throw new UserNotLoginException();
        }
        if(videoPublishDto.getVideoUrl() == null || videoPublishDto.getVideoUrl().isEmpty()){
            throw new NullParamException();
        }
        //分区大于9不合规范，设为默认0
        if(videoPublishDto.getSection()>9){
            videoPublishDto.setSection(0);
        }
        //默认封面
        if(videoPublishDto.getCoverUrl()==null||videoPublishDto.getCoverUrl().isEmpty()){
            videoPublishDto.setCoverUrl(videoPublishDto.getVideoUrl()+"?vframe/jpg/offset/0");
        }
        //保存视频到数据库
        Video video= Video.builder().
                videoUrl(videoPublishDto.getVideoUrl()).
                coverUrl(videoPublishDto.getCoverUrl()).
                authorId(userId).
                //section默认为0
                section(videoPublishDto.getSection()).
                //标题默认为"视频"
                title(videoPublishDto.getTitle()!=null?videoPublishDto.getTitle():"视频").
                //状态默认为正常
                status(0).
                likes(0L).
                collects(0L).
                comments(0L).
                createTime(LocalDateTime.now()).
                build();
        try {
            videoMapper.insert(video);
            rocketMQTemplate.convertAndSend("video_publish",video);
            //将视频存储在对应videoId下
            String videoKey=VideoConstant.VIDEO_ID+video.getId().toString();
            stringRedisTemplate.opsForValue().set(videoKey, JSON.toJSONString(video));
            stringRedisTemplate.opsForValue().set(VideoConstant.LAST_VIDEO_ID,video.getId().toString());
            //存储userId下的video类
            stringRedisTemplate.opsForList().leftPush(VideoConstant.USER_VIDEO_LIST+userId,video.getId().toString());
        }catch (Exception e){
            log.error("保存视频信息失败{}",e.getMessage());
            throw new DbOperationException("保存视频信息失败！");
        }
        VideoUploadVo videoUploadVo=new VideoUploadVo();
        BeanUtils.copyProperties(video,videoUploadVo);
        return ResponseResult.successResult(videoUploadVo);

    }

    /**
     * 上传视频
     * @param file 视频文件
     * @return 是否成功
     */
    @Override
    public ResponseResult<String> upload(MultipartFile file){
        log.info("文件上传 ：{}",file);
        String filePath;
        //参数校验
        if(file == null){
            log.error("文件为空");
            throw new NullParamException("文件为空");
        } else {
            //生成文件名并上传文件
            try {
                filePath = QiniuOssUtil.upload(file.getBytes(),dealFileName(file));
            } catch (Exception e){
                log.error("文件上传失败{}",e.getMessage());
                throw new QiniuException();
            }
        }
        return ResponseResult.successResult(filePath);
    }

    /**
     * 获取视频,一次得10个
     * 在发布视频时根据VIDEO_LIST_KEY+视频id/11将视频存入相应的list中，这样每个list都会有10条视频
     * 在每个用户观看视频时根据NOW_List_ID+useId键在redis中取出对应id的list
     * @param lastVideoId 最后一个视频的id
     * @return 返回response
     */
    @Override
    public ResponseResult<GetVideoInfo> getVideos(Integer lastVideoId,Integer section) {
        GetVideoInfo getVideoInfo = new GetVideoInfo();
        List<VideoDetailInfo>videoList = new ArrayList<>();
        if(lastVideoId==0){
            lastVideoId=Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(VideoConstant.LAST_VIDEO_ID)));
        }
        for (Integer i = lastVideoId; i > lastVideoId - 10; i--) {
            VideoDetailInfo videoDetailInfo=new VideoDetailInfo();
            //i就是videoId
            //得到video对象
            Video video = getVideoById(i);
            //如果没视频了，把之前的视频返回
            if(video==null){
                getVideoInfo.setVideoList(videoList);
                getVideoInfo.setLastVideoId(i);
                getVideoInfo.setTotal(videoList.size());
                return ResponseResult.successResult(getVideoInfo);
            }
            if(!Objects.equals(section,video.getSection())&&!Objects.equals(section,0)){
                continue;
            }
            BeanUtils.copyProperties(video,videoDetailInfo);
            //获取时间
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String format = video.getCreateTime().format(df);
            videoDetailInfo.setCreateTime(format);
            //判断是否喜欢与收藏
            videoDetailInfo.setLiked
                    (isDo(i.longValue(),VideoConstant.SET_LIKE_KEY+video.getId().toString()));
            videoDetailInfo.setCollected
                    (isDo(i.longValue(),VideoConstant.SET_LIKE_KEY+video.getId().toString()));
            //得到作者信息
            ResponseResult<UserPersonalInfoVo> res = userClient.getUserPersonalInfo(video.getAuthorId());
            UserPersonalInfoVo user = res.getData();
            videoDetailInfo.setUserName(user.getUsername());
            videoDetailInfo.setImage(user.getImage());
            videoList.add(videoDetailInfo);
        }
        getVideoInfo.setVideoList(videoList);
        getVideoInfo.setTotal(videoList.size());
        getVideoInfo.setLastVideoId(lastVideoId-10);
        return ResponseResult.successResult(getVideoInfo);
    }

    /**
     * 处理文件名 uuid+文件名+后缀
     * @param file 传过来文件
     * @return 处理后的文件名
     */
    private String dealFileName(MultipartFile file){
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ErrorParamException("文件名为空");
        }
        //截取原始文件名后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }

    /**
     * 通过videoId得到video的实体类
     * @param videoId 视频id
     * @return 视频实体类
     */
    @Override
    public Video getVideoById(Integer videoId){
        //得到video对象
        String s = stringRedisTemplate.opsForValue().get(VideoConstant.VIDEO_ID + videoId.toString());
        if(s==null){
            return null;
        }
        //将redis中的数据反序列化为对象
        Video video = JSON.parseObject(s, Video.class);
        //获取点赞数，评论数，收藏数
        String likes = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_LIKE_KEY + videoId);
        if(likes==null){
            //要是发现redis中like字段过期，
            // 则从数据库中查询数据返回，并同时把此视频所有字段刷新到redis
            likes=dbOpsService.getSumFromDb(videoId.longValue()).toString();
        }
        String collects = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COLLECT_KEY + videoId);
        String comments = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COMMENT_KEY + videoId);
        video.setLikes(Long.parseLong(likes));
        video.setCollects(collects == null ? 0 : Long.parseLong(collects));
        video.setComments(comments == null ? 0 : Long.parseLong(comments));
        return video;
    }

    /**
     * 获取视频信息
     * @param videoId 视频id
     * @return 视频信息
     */
    @Override
    public ResponseResult<VideoInfo> getVideoInfo(Long videoId) {
        Video video = getVideoById(videoId.intValue());
        VideoInfo videoInfo=new VideoInfo();
        videoInfo.setVideoId(video.getId());
        BeanUtils.copyProperties(video,videoInfo);
        return ResponseResult.successResult(videoInfo);
    }

    /**
     * 获取视频详细信息
     * @param videoId 视频id
     * @return 视频详细信息
     */
    @Override
    public ResponseResult<VideoDetail> getVideoDetailInfo(Long videoId) {
        log.info("获取视频详细信息，视频id：{}",videoId);
        VideoDetail videoDetailInfo=new VideoDetail();
        Video video = getVideoById(videoId.intValue());
        //拷贝数据
        BeanUtils.copyProperties(video,videoDetailInfo);
        videoDetailInfo.setAuthorId(video.getAuthorId().toString());
        videoDetailInfo.setVideoId(video.getId().toString());
        //判断是否喜欢与收藏
        videoDetailInfo.setIsLiked
                (isDo(videoId,VideoConstant.SET_LIKE_KEY+video.getId().toString()));
        videoDetailInfo.setIsCollected
                (isDo(videoId,VideoConstant.SET_LIKE_KEY+video.getId().toString()));
        //得到作者信息
        log.info("获取作者信息，作者id：{}",video.getAuthorId());
        ResponseResult<UserPersonalInfoVo> res = userClient.getUserPersonalInfo(video.getAuthorId());
        log.info("获取作者信息，结果：{}",res);
        UserPersonalInfoVo user = res.getData();
        videoDetailInfo.setUserName(user.getUsername());
        videoDetailInfo.setImage(user.getImage());
        return ResponseResult.successResult(videoDetailInfo);
    }

    /**
     * 判断是否点赞或收藏
     * @param videoId 视频id
     * @param setLikeKey set集合的key
     * @return 是否点赞或收藏
     */
    public boolean isDo(Long videoId,String setLikeKey){
        Long userId = ThreadLocalUtil.getUserId();
        //判断key是否存在
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(setLikeKey))){
            //如果userId在set里，说明已经点赞
            return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(setLikeKey, userId.toString()));
        }
        //key不存在就在mongo里面找
        else{
            //查询当用户id和视频id所在的字段
            Query query=Query.query
                    (Criteria.where("userId").is(userId.toString()).
                            and("videoId").is(videoId.toString()));
            VideoLike videoLike = mongoTemplate.findOne(query, VideoLike.class);
            //如果此字段不存在，直接返回0
            return videoLike != null && videoLike.getIsLike() != 0;
        }
    }
}

