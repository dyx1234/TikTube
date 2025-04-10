package com.azaz.interact.vo;

import com.azaz.user.vo.UserPersonalInfoVo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户列表Vo
 * @author shigc
 */
@Data
@Builder
public class UserListVo {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 用户列表
     */
    private List<UserPersonalInfoVo> list;

}
