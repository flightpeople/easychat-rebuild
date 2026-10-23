package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 邀请入群响应（指令 13）：推送给【被邀请人】。
 * inviteId = 邀请人 userId（客户端据此知道是谁拉的我）。
 */
public class InviteGroupRespPacket extends Packet {

    private String groupName;
    private String groupId;
    private String inviteId;

    @Override
    public Byte getCommand() {
        return Command.INVITE_GROUP_RESPONSE;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }
}
