package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

import java.util.List;

/**
 * 创建群请求（指令 10）。{groupName, users}——users 是建群时想拉的人，
 * 与原版一致：拉人仍要走“邀请-接受”闭环，这里只建群 + 群主入群。
 */
public class CreateGroupReqPacket extends Packet {

    private String groupName;
    private List<String> users;

    @Override
    public Byte getCommand() {
        return Command.CREATE_GROUP_REQUEST;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
