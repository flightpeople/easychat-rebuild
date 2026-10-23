package com.example.easychat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** 群成员关系实体（M5a）。 */
@TableName("sp_user_group")
public class GroupMember {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupId;
    private String userId;

    public GroupMember() {
    }

    public GroupMember(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
