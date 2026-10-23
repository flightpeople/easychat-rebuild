package com.example.easychat.packet.message;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 单聊消息自响应（指令 25）：投递失败时回给【发送方自己】。
 * 失败原因两类：非好友（不给发）、对方不在线（原版语义）。
 * 成功时发送方不收自响应——对方回复本身就是送达证明（省一半包）。
 */
public class MessageSelfRespPacket extends Packet {

    private String receiverId;
    private String groupId;
    private boolean success;
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_SELF_RESPONSE;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
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
