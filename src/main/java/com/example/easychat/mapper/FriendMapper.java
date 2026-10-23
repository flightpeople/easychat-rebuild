package com.example.easychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.easychat.model.UserFriend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友关系 Mapper（M4）。
 *
 * BaseMapper 提供单表 CRUD；“查我的好友列表”需要联表 sp_user 拿好友的
 * 用户名/昵称（客户端要显示“张三(zhangsan)”而不只是一串 ID）——
 * 这种跨表查询用 @Select 自定义 SQL（MyBatis 的经典用法）。
 */
@Mapper
public interface FriendMapper extends BaseMapper<UserFriend> {

    /**
     * 查我的好友列表（联表带出好友的用户信息）。
     * 面试点：为什么好友关系要双向冗余存储？这条 SQL 就是答案——
     * 单向存储的话这里要 OR 两个方向 + 判断归属，索引也用不上。
     */
    @Select("SELECT u.* FROM sp_user u JOIN sp_user_friend f ON u.user_id = f.friend_id "
            + "WHERE f.user_id = #{userId}")
    List<com.example.easychat.model.User> selectFriendsWithInfo(@Param("userId") String userId);
}
