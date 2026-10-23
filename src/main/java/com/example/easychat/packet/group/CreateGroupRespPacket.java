package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/** 创建群响应（指令 11）：回给建群者。 */
public class CreateGroupRespPacket extends Packet {

    private String groupName;
    private String groupId;
    private boolean success;
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.CREATE_GROUP_RESPONSE;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
