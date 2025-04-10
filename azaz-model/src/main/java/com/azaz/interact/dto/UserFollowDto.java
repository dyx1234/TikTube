package com.azaz.interact.dto;

import lombok.Data;

/**
 * 关注dto
 * @author shigc
 */
@Data
public class UserFollowDto {

    /**
     * 操作目标用户id
     */
    private Long userId;

    /**
     * 操作类型 1关注 0取消关注
     */
    private Integer type;
}
