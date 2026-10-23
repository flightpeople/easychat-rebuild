package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 入群审批请求（指令 32）：{groupId, userId=申请人, allow}——只有群主能发。
 */
public class AllowGroupReqPacket extends Packet {

    private String groupId;
    private String userId;
    private boolean allow;

    @Override
    public Byte getCommand() {
        return Command.ALLOW_GROUP_REQUEST;
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

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean allow) {
        this.allow = allow;
    }
}
