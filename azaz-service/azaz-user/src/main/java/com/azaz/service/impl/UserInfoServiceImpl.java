package com.azaz.service.impl;

import com.alibaba.fastjson.JSON;
import com.azaz.clients.InteractClient;
import com.azaz.clients.VideoClient;
import com.azaz.constant.UserConstant;
import com.azaz.exception.ErrorParamException;
import com.azaz.exception.UserNotExitedException;
import com.azaz.exception.UserNotLoginException;
import com.azaz.mapper.UserMapper;
import com.azaz.response.ResponseResult;
import com.azaz.user.dto.AckPasswordDto;
import com.azaz.user.dto.UserPersonInfoDto;
import com.azaz.user.pojo.User;
import com.azaz.user.vo.UserHomePageVo;
import com.azaz.user.vo.UserPersonalInfoVo;
import com.azaz.utils.QiniuOssUtil;
import com.azaz.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息服务实现类
 * @author shigc
 */
@Service
@Log4j2
public class UserInfoServiceImpl implements com.azaz.service.UserInfoService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private InteractClient interactClient;

    @Resource
    private VideoClient videoClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult
     */
    @Override
    public ResponseResult<UserPersonalInfoVo> getUserPersonalInfo(Long userId) {
        //  获取到用户的id,如果为空,则说明是查自己，从ThreadLocal中获取
        if (userId == null){
            userId = ThreadLocalUtil.getUserId();
        }
        log.info("用户个人信息查询: {}", userId);
        //  校验用户id是否为空
        if (userId == null){
            throw new UserNotLoginException();
        }
        // 从 redis 中获取用户信息
        String userInfoRedis = stringRedisTemplate.opsForValue().get(UserConstant.REDIS_USER_INFO + userId);
        if (userInfoRedis != null){
            // redis 中存在用户信息，直接返回
            UserPersonalInfoVo userPersonalInfoVo = JSON.parseObject(userInfoRedis, UserPersonalInfoVo.class);
            return ResponseResult.successResult(userPersonalInfoVo);
        }
        //  根据用户id查询用户信息, 只需要 用户名、头像、签名
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("username", "image", "signature").eq("id", userId);
        User user = userMapper.selectOne(queryWrapper);
        // 校验用户信息是否为空
        if (user == null){
            throw new UserNotExitedException();
        }
        // 拷贝属性
        UserPersonalInfoVo userPersonalInfoVo = new UserPersonalInfoVo();
        BeanUtils.copyProperties(user, userPersonalInfoVo);
        userPersonalInfoVo.setId(String.valueOf(userId));
        // 将用户信息存入 redis
        stringRedisTemplate.opsForValue().set(UserConstant.REDIS_USER_INFO + userId, JSON.toJSONString(userPersonalInfoVo),
                UserConstant.REDIS_USER_INFO_TTL, TimeUnit.SECONDS);
        // 封装返回结果
        return ResponseResult.successResult(userPersonalInfoVo);
    }

    /**
     * 更新用户个人信息
     * @param userPersonInfo 用户个人信息
     * @return ResponseResult
     */
    @Override
    public ResponseResult updateUserPersonalInfo(UserPersonInfoDto userPersonInfo) {
        log.info("用户个人信息更新: {}", userPersonInfo);
        // 0. 校验参数
        if (userPersonInfo == null || userPersonInfo.getSignature() == null ||
                userPersonInfo.getImage() == null || userPersonInfo.getUsername() == null){
            throw new ErrorParamException("参数不能为空！");
        }
        if (userPersonInfo.getUsername().length() > 15) {
            throw new ErrorParamException("用户名不能超过15个字符！");
        }
        if (userPersonInfo.getSignature().length() > 100) {
            throw new ErrorParamException("签名不能超过100个字符！");
        }
        // 1. 获取到用户的id
        Long userId = ThreadLocalUtil.getUserId();
        // 1.1 校验用户id是否为空
        if (userId == null){
            throw new UserNotLoginException();
        }
        // 2. 更新用户信息
        //从redis中获取删除信息
        stringRedisTemplate.delete(UserConstant.REDIS_USER_INFO + userId);
        User user = new User();
        BeanUtils.copyProperties(userPersonInfo, user);
        user.setId(userId);
        try {
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("用户信息更新失败: {}", e.getMessage());
            throw new ErrorParamException("用户不存在！");
        }
        // 3. 发送消息到消息队列,ES索引库中更新用户信息
        rocketMQTemplate.convertAndSend("user_info", user);
        return ResponseResult.successResult();
    }

    /**
     * 上传用户头像
     * @param imageFile 用户头像文件
     * @return ResponseResult<String> 图片地址
     */
    @Override
    public ResponseResult<String> uploadUserImage(MultipartFile imageFile) {
        //1. 获取文件名
        //1.1 校验文件是否为空
        if (imageFile == null){
            throw new ErrorParamException("头像不能为空！");
        }
        String fileName = imageFile.getOriginalFilename();
        if (fileName == null){
            throw new ErrorParamException("不支持的文件类型！");
        }
        //2. 分别获取文件后缀与文件名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //2.1 检查是否是想要的文件类型
        if (!".jpg".equals(suffix) && !".png".equals(suffix) && !".jpeg".equals(suffix) ){
            throw new ErrorParamException("不支持的文件类型！");
        }
        //2.2 拼接文件名
        String name = UUID.randomUUID() + suffix;
        //3. 上传文件
        String url = null;
        try {
            url = QiniuOssUtil.upload(imageFile.getBytes(), name);
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage());
        }
        return ResponseResult.successResult(url);
    }

    /**
     *  获取用户主页信息
     * @param userId 用户id
     * @return ResponseResult<UserHomePageVo> 用户主页信息
     */
    @Override
    public ResponseResult<UserHomePageVo> getUserHomePage(Long userId) {
        // 1. 获取基本信息
        ResponseResult<UserPersonalInfoVo> userPersonalInfoVoResponseResult = getUserPersonalInfo(userId);
        if (userPersonalInfoVoResponseResult == null || userPersonalInfoVoResponseResult.getData() == null){
            throw new UserNotExitedException();
        }
        UserPersonalInfoVo userPersonalInfo = userPersonalInfoVoResponseResult.getData();
        // 2. 获取关注数
        Integer followNum = interactClient.getFollowNum(userId).getData();
        // 3. 获取粉丝数
        Integer fansNum = interactClient.getFansNum(userId).getData();
        Integer workNums = videoClient.getUserWorks(userId).getData();
        Integer likeNums = videoClient.getUserLikes(userId).getData();
        // 4. 获取是否关注
        Boolean isFollow = interactClient.ifFollow(ThreadLocalUtil.getUserId(), userId).getData();
        UserHomePageVo userHomePageVo = UserHomePageVo.builder()
                .id(userId)
                .username(userPersonalInfo.getUsername())
                .image(userPersonalInfo.getImage())
                .signature(userPersonalInfo.getSignature())
                .fansNum(fansNum)
                .followNum(followNum)
                .workNum(workNums)
                .likedNum(likeNums)
                .isFollow(isFollow)
                .build();
        return ResponseResult.successResult(userHomePageVo);
    }

    /**
     * 用户密码确认
     * @param ackPasswordDto 用户密码传输对象
     * @return ResponseResult<Boolean> 确认结果
     */
    @Override
    public ResponseResult<Boolean> ackPassword(AckPasswordDto ackPasswordDto) {
        // 校验参数
        if (ackPasswordDto == null || ackPasswordDto.getPassword() == null || ackPasswordDto.getUserId() == null){
            throw new ErrorParamException("参数不能为空！");
        }
        // 从数据库获取用户信息
        User user = userMapper.selectById(ackPasswordDto.getUserId());
        if (user == null){
            throw new UserNotExitedException();
        }
        // 校验密码是否正确
        String passwordWithMd5 = DigestUtils.md5DigestAsHex((ackPasswordDto.getPassword() + user.getSalt()).getBytes());
        if (!passwordWithMd5.equals(user.getPassword())){
            throw new ErrorParamException("密码错误！");
        }
        return ResponseResult.successResult(true);
    }
}