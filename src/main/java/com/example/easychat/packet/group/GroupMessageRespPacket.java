package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 群聊消息响应（指令 16）：推送给群里的【每个成员】。
 * 比单聊响应多 groupId 和 avatar——群消息要显示“哪个群、谁说的、头像”。
 */
public class GroupMessageRespPacket extends Packet {

    private String senderId;
    private String senderName;
    private String avatar;
    private String groupId;
    private String message;
    private int messageType = 0;

    @Override
    public Byte getCommand() {
        return Command.GROUP_MESSAGE_RESPONSE;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
