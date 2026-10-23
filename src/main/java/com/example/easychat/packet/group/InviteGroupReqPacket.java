package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

import java.util.List;

/**
 * 邀请入群请求（指令 12）：{groupId, users[]}——批量拉人。
 */
public class InviteGroupReqPacket extends Packet {

    private String groupId;
    private List<String> users;

    @Override
    public Byte getCommand() {
        return Command.INVITE_GROUP_REQUEST;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
