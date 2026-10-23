package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 接受入群响应（指令 18）：双方都收。
 * acceptUser = 接受入群的那个用户；邀请方收到后可以刷新群成员列表。
 */
public class AcceptGroupRespPacket extends Packet {

    private String acceptUser;
    private String groupId;
    private boolean success;

    @Override
    public Byte getCommand() {
        return Command.ACCEPT_GROUP_RESPONSE;
    }

    public String getAcceptUser() {
        return acceptUser;
    }

    public void setAcceptUser(String acceptUser) {
        this.acceptUser = acceptUser;
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
}
