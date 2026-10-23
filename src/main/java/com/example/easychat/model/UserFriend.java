package com.example.easychat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 好友关系实体（M4）。
 *
 * 双向冗余存储：A 和 B 成为好友，表里有两条记录（A→B 和 B→A）。
 * 为什么不用一条记录 + 查询时 OR 两个方向？
 * - 查“我的好友”一条 SQL 搞定（where user_id = me），不需要 UNION/or 条件；
 * - 关系带上归属者的属性（remark 备注：我给 B 的备注 ≠ B 给我的备注）；
 * - 代价是写入翻倍、要保证两条记录的事务一致性（M4 用简单双插，生产应包事务）。
 */
@TableName("sp_user_friend")
public class UserFriend {

    /** 自增主键——和 user_id 的随机串策略对比：关系表 ID 不对外暴露，自增无妨 */
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String friendId;
    private String remark;

    public UserFriend() {
    }

    public UserFriend(String userId, String friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
