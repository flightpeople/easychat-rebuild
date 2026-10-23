package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/** 申请入群请求（指令 29）：{groupId}——发给群主审批。 */
public class ApplyGroupReqPacket extends Packet {

    private String groupId;

    @Override
    public Byte getCommand() {
        return Command.APPLY_GROUP_REQUEST;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
