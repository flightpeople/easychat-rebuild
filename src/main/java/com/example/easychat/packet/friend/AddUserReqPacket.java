package com.example.easychat.packet.friend;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 添加好友请求（指令 7）：A 想加 B。
 * 字段名 addUserId 与原版一致（协议兼容）——“要添加的用户ID”。
 */
public class AddUserReqPacket extends Packet {

    private String addUserId;

    @Override
    public Byte getCommand() {
        return Command.ADD_USER_REQUEST;
    }

    public String getAddUserId() {
        return addUserId;
    }

    public void setAddUserId(String addUserId) {
        this.addUserId = addUserId;
    }
}
