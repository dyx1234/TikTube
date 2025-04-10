package com.azaz.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.azaz.constant.UserConstant;
import com.azaz.constant.UserDefaultImageConstant;
import com.azaz.exception.*;
import com.azaz.mapper.UserLoginMapper;
import com.azaz.mapper.UserMapper;
import com.azaz.response.ResponseResult;
import com.azaz.user.dto.RegisterDto;
import com.azaz.user.dto.UserLoginDto;
import com.azaz.user.pojo.User;
import com.azaz.user.vo.UserLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 用户登录服务接口
 * @Author shigc
 */
@Service
@Log4j2
public class UserLoginServiceImpl implements com.azaz.service.UserLoginService{

    @Resource
    private UserLoginMapper userLoginMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用户注册
     * @param registerDto 注册信息
     * @return 注册结果
     */
    @Override
    public ResponseResult register(RegisterDto registerDto) {
        log.info("用户注册，注册信息：{}", registerDto);
        // 1. 校验参数
        if (registerDto == null || registerDto.getPhone() == null || registerDto.getPassword() == null) {
            throw new NullParamException("注册信息不能为空！");
        }
        String phone = registerDto.getPhone();
        String password = registerDto.getPassword();
        // 1.1 校验手机号是否合法
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX, phone)) {
            throw new ErrorParamException("手机号不合法！");
        }
        // 1.2 校验密码是否合法
        if (password.length() < UserConstant.PASSWORD_MIN_LENGTH || password.length() > UserConstant.PASSWORD_MAX_LENGTH) {
            throw new ErrorParamException("密码长度必须在5~20位之间！");
        }
        // 对手机号加锁，防止并发注册
        RLock registerLock = redissonClient.getLock(UserConstant.USER_REGISTER_LOCK + phone);
        boolean tryLock = registerLock.tryLock();
        if (!tryLock) {
            // 出现并发注册，返回错误信息
            throw new ErrorParamException("注册失败，请稍后再试！");
        }
        try {
            // 校验验证码是否正确
            // 校验手机号是否已经注册, 即手机号对应的记录数是否大于0
            if (userLoginMapper.ifExistPhone(phone) > 0) {
                throw new ErrorParamException("手机号已经注册！");
            }
            // 注册用户
            // 随机生成 5 位长度的盐
            String salt = RandomUtil.randomString(5);
            // 对密码进行加密
            String passwordWithMd5 = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            User user = User.builder()
                    .phone(phone)
                    .salt(salt)
                    .password(passwordWithMd5)
                    .username(UserConstant.DEFAULT_USER_NAME_PRE + RandomUtil.randomString(10))
                    .image(UserDefaultImageConstant.DEFAULT_USER_IMAGE_LIST[RandomUtil.randomInt(0, UserDefaultImageConstant.DEFAULT_USER_IMAGE_LIST.length)])
                    .signature(UserConstant.DEFAULT_USER_SIGNATURE)
                    .build();
            // 将用户信息插入数据库
            try {
                userMapper.insert(user);
            } catch (Exception e) {
                log.error("注册信息插入错误，注册信息：{}", registerDto);
                throw new DbOperationException("注册信息插入错误！");
            }
            // 返回注册成功
            return ResponseResult.successResult();
        } finally {
            registerLock.unlock();
        }
    }

    /**
     *  用户登录
     * @param userLoginDto 登录信息
     * @return 登录结果
     */
    @Override
    public ResponseResult<UserLoginVo> userLogin(UserLoginDto userLoginDto) {
        log.info("用户登录，登录信息：{}", userLoginDto);
        // 1. 校验参数
        if (userLoginDto == null || userLoginDto.getPhone() == null || userLoginDto.getPassword() == null) {
            throw new NullParamException("登录信息不能为空！");
        }
        String phone = userLoginDto.getPhone();
        String password = userLoginDto.getPassword();
        // 1.1 校验手机号是否合法
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX, phone)) {
            throw new ErrorParamException("手机号不合法！");
        }
        // 1.2 校验密码是否合法
        if (password.length() < UserConstant.PASSWORD_MIN_LENGTH || password.length() > UserConstant.PASSWORD_MAX_LENGTH) {
            throw new ErrorParamException("密码错误！");
        }
        // 2. 从数据库获取用户信息
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User userDb = userMapper.selectOne(wrapper);
        // 3. 校验用户是否存在
        if (userDb == null) {
            throw new UserNotExitedException();
        }
        // 4. 校验密码是否正确
        String passwordWithMd5 = DigestUtils.md5DigestAsHex((password + userDb.getSalt()).getBytes());
        if (!StrUtil.equals(passwordWithMd5, userDb.getPassword())) {
            // 4.1 密码错误，返回错误信息
            throw new PasswordErrorException();
        }
        // 5. 登录成功，Redis中保存token， 返回用户信息
        // 5.1 生成用户token
        String token = RandomUtil.randomString(32);
        // 5.2 保存用户token到Redis,设置过期时间为 2 小时
        stringRedisTemplate.opsForValue().set(UserConstant.REDIS_LOGIN_TOKEN + token, userDb.getId().toString(),
                UserConstant.LOGIN_USER_TTL, java.util.concurrent.TimeUnit.SECONDS);
        // 5.3 返回用户信息
        UserLoginVo userLoginVo = new UserLoginVo(token, userDb.getId().toString());
        return ResponseResult.successResult(userLoginVo);
    }

}
