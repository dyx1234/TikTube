package com.azaz.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户主页信息vo
 * @author shigc
 */
@Data
@Builder
public class UserHomePageVo {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像地址
     */
    private String image;

    /**
     * 签名
     */
    private String signature;

    /**
     * 粉丝数
     */
    private Integer fansNum;

    /**
     * 关注数
     */
    private Integer followNum;

    /**
     * 作品数
     */
    private Integer workNum;

    /**
     * 获赞数
     */
    private Integer likedNum;

    /**
     * 是否关注
     */
    private Boolean isFollow;

}
