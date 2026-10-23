package com.example.easychat.packet.friend;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 接受好友响应（指令 20）：B 同意后，服务端同时推给【双方】——
 * 邀请方 A 收到“对方同意了”，接受方 B 收到“操作成功确认”。
 * 这就是原版文档写的“服务端发送接受好友响应消息到好友添加邀请方”。
 */
public class AcceptRespPacket extends Packet {

    private String inviterId;
    private String invitedId;
    private String invitedName;
    private boolean success;

    @Override
    public Byte getCommand() {
        return Command.ACCEPT_FRIEND_RESPONSE;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInvitedId() {
        return invitedId;
    }

    public void setInvitedId(String invitedId) {
        this.invitedId = invitedId;
    }

    public String getInvitedName() {
        return invitedName;
    }

    public void setInvitedName(String invitedName) {
        this.invitedName = invitedName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
