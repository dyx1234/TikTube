package com.azaz.clients;


import com.azaz.interceptor.MyFeignRequestInterceptor;
import com.azaz.response.ResponseResult;
import com.azaz.video.vo.VideoDetail;
import com.azaz.video.vo.VideoInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 视频客户端
 * @author shigc
 */
@FeignClient(value = "azaz-service-video", configuration = MyFeignRequestInterceptor.class)
public interface VideoClient {

    /**
     * 得到用户被赞数
     * @param userId 用户id
     * @return 被赞数
     */
    @GetMapping("/azaz/video/getUserLikes")
    ResponseResult<Integer> getUserLikes(@RequestParam("userId") Long userId);

    /**
     * 得到用户作品总数
     * @param userId 用户id
     * @return 作品总数
     */
    @GetMapping("/azaz/video/getUserWorks")
    ResponseResult<Integer> getUserWorks(@RequestParam("userId")Long userId);

    /**
     * 得到视频简略信息
     * @param videoId 视频id
     * @return 视频简略信息
     */
    @GetMapping("/azaz/video/info")
    ResponseResult<VideoInfo> getVideoInfo(@RequestParam("videoId") Long videoId);

    /**
     * 得到视频详细信息
     * @param videoId 视频id
     * @return 视频详细信息
     */
    @GetMapping("/azaz/video/detailInfo")
    ResponseResult<VideoDetail> getVideoDetailInfo(@RequestParam("videoId") Long videoId);
}
