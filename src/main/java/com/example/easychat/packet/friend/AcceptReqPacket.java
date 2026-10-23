package com.example.easychat.packet.friend;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 接受好友请求（指令 19）：B 回复服务端。
 * receiver = 邀请方 A 的 userId；accept = 同意/拒绝。
 */
public class AcceptReqPacket extends Packet {

    private String receiver;
    private boolean accept;

    @Override
    public Byte getCommand() {
        return Command.ACCEPT_FRIEND_REQUEST;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
