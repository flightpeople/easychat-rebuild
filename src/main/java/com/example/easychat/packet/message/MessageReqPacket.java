package com.example.easychat.packet.message;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 单聊消息请求（指令 5）。{receiver=对方userId, message, messageType=0文本}。
 */
public class MessageReqPacket extends Packet {

    private String receiver;
    private String message;
    private int messageType = 0;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_REQUEST;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
