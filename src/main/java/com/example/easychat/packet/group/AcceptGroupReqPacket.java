package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/** 接受入群请求（指令 17）：{groupId, inviterId, accept}。 */
public class AcceptGroupReqPacket extends Packet {

    private String groupId;
    private String inviterId;
    private boolean accept;

    @Override
    public Byte getCommand() {
        return Command.ACCEPT_GROUP_REQUEST;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
