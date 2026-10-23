package com.example.easychat.packet.message;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 单聊消息响应（指令 6）：推送给【接收方】。
 * 注意字段叫 sender/senderName 而不是 fromUserId——接收方视角的命名：
 * 客户端渲染“张三: 你好”直接能用，不用再查一次用户表。
 */
public class MessageRespPacket extends Packet {

    private String sender;
    private String senderName;
    private String message;
    private int messageType = 0;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_RESPONSE;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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
