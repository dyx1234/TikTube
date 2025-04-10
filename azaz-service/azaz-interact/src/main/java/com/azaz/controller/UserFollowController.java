package com.azaz.controller;


import com.azaz.interact.dto.UserFollowDto;
import com.azaz.response.ResponseResult;
import com.azaz.service.UserFollowService;
import com.azaz.user.vo.UserPersonalInfoVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 关注
 * @author shigc
 */
@RestController
@RequestMapping("/azaz/interact/follow")
public class UserFollowController {
    /**
     * 关注服务
     */
    @Resource
    private UserFollowService userFollowService;

    /**
     * 关注或者取消关注
     * @param userFollowDto 关注信息
     * @return 关注结果
     */
    @PostMapping("/do")
    public ResponseResult doFollow(UserFollowDto userFollowDto) {
        return userFollowService.doFollow(userFollowDto);
    }

    /**
     * 判断是否关注
     * @param firstUser 第一个用户
     * @param secondUser 第二个用户
     * @return 是否关注
     */
    @GetMapping("/ifFollow")
    public ResponseResult<Boolean> ifFollow(@RequestParam("firstUser") Long firstUser,
                                            @RequestParam("secondUser") Long secondUser) {
        return userFollowService.ifFollow(firstUser, secondUser);
    }

    /**
     * 判断是否互关
     * @param firstUser  第一个用户
     * @param secondUser  第二个用户
     * @return 是否互关
     */
    @GetMapping("/ifFollowEachOther")
    public ResponseResult<Boolean> ifFollowEachOther(@RequestParam("firstUser") Long firstUser,
                                                     @RequestParam("secondUser") Long secondUser) {
        return userFollowService.ifFollowEachOther(firstUser, secondUser);
    }

    /**
     * 获取互关朋友列表
     * @return 互关朋友列表
     */
    @GetMapping("/friends")
    public ResponseResult<List<UserPersonalInfoVo>> getFriends() {
        return userFollowService.getFriends();
    }

    /**
     * 获取关注列表
     * @return 关注列表
     */
    @GetMapping("/list")
    public ResponseResult<List<UserPersonalInfoVo>> getFollowList() {
        return userFollowService.getFollowList();
    }

    /**
     * 获取关注总数
     * @return 关注总数
     */
    @GetMapping("/num")
    public ResponseResult<Integer> getFollowNum(@RequestParam("userId") Long userId) {
        return userFollowService.getFollowNum(userId);
    }

    /**
     * 获取粉丝数
     * @param userId 用户id
     * @return 粉丝数
     */
    @GetMapping("/fansNum")
    public ResponseResult<Integer> getFansNum(@RequestParam("userId") Long userId) {
        return userFollowService.getFansNum(userId);
    }
}
