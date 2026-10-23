package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 群聊消息请求（指令 15）。{groupId, message, messageType}。
 * 和单聊(5)的区别：接收方是“群”而不是“人”——服务端要扇出给每个成员。
 */
public class GroupMessageReqPacket extends Packet {

    private String groupId;
    private String message;
    private int messageType = 0;

    @Override
    public Byte getCommand() {
        return Command.GROUP_MESSAGE_REQUEST;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
