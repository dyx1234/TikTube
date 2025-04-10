package com.azaz.mapper;

import com.azaz.interact.pojo.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 私信mapper
 * @Author shigc
 */
@Mapper
public interface PrivateMessageMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<PrivateMessage> {
    /**
     * 根据发送者id统计私信数量
     * @param senderId 发送者id
     * @param receiverId 接收者id
     * @return 私信数量
     */
    @Select("select count(*) from azaz.tb_private_message where sender_id = #{senderId} and receiver_id = #{receiverId}")
    Integer countBySenderId(@Param("senderId") Long senderId,@Param("receiverId") Long receiverId);

    /**
     * 根据用户id和好友id查询私信列表
     * @param userId 用户id
     * @param friendId 好友id
     * @param lastMessageId 最后一条私信id
     * @return 私信列表
     */
    List<PrivateMessage> selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId")Long friendId,
                                                   @Param("lastMessageId") Long lastMessageId);
}
