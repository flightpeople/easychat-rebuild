package com.example.easychat.packet.friend;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 添加好友自响应（指令 9）：服务端回给【邀请方 A 自己】。
 * 为什么同一个动作要回两种消息？A 需要知道自己发起的请求到达了没有
 * （对方在不在线、有没有发出去）——这就是“自响应”的设计动机：
 * 操作方永远对自己的操作结果有知情权。
 */
public class AddUserSelfRespPacket extends Packet {

    private String invitedId;
    private String reason;
    private boolean success;

    @Override
    public Byte getCommand() {
        return Command.ADD_USER_SELF_RESPONSE;
    }

    public String getInvitedId() {
        return invitedId;
    }

    public void setInvitedId(String invitedId) {
        this.invitedId = invitedId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
