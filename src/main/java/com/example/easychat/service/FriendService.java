package com.example.easychat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.easychat.mapper.FriendMapper;
import com.example.easychat.model.User;
import com.example.easychat.model.UserFriend;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 好友服务（M4）。
 *
 * 职责：好友关系的“账本”——谁是谁的好友、添加流程的状态流转。
 * 注意：本服务不负责“把消息送到对方手里”（那是 Handler 用 SessionUtil 干的活），
 * 服务层只碰数据库，Handler 层碰网络——分层边界。
 */
@Service
public class FriendService {

    private final FriendMapper friendMapper;
    private final UserService userService;

    public FriendService(FriendMapper friendMapper, UserService userService) {
        this.friendMapper = friendMapper;
        this.userService = userService;
    }

    /** 是否已是好友（幂等判断：重复添加要拦截） */
    public boolean isFriend(String userId, String friendId) {
        return friendMapper.exists(new LambdaQueryWrapper<UserFriend>()
                .eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId));
    }

    /**
     * 建立双向好友关系：插两条（A→B、B→A）。
     * 生产环境这两条插入应包在同一个事务里（@Transactional），
     * 否则插第一条成功第二条失败会出现“单向好友”脏数据。
     */
    public void createFriendship(String userId, String friendId) {
        friendMapper.insert(new UserFriend(userId, friendId));
        friendMapper.insert(new UserFriend(friendId, userId));
    }

    public List<User> listFriends(String userId) {
        return friendMapper.selectFriendsWithInfo(userId);
    }

    /** 更新对某个好友的备注（只更新自己这条关系记录，不影响对方） */
    public void updateRemark(String userId, String friendId, String remark) {
        friendMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserFriend>()
                .eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId)
                .set(UserFriend::getRemark, remark));
    }

    public User getUser(String userId) {
        return userService.getById(userId);
    }
}
