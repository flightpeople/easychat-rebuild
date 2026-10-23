package com.example.easychat.packet.friend;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 添加好友响应（指令 8）：服务端推送给【被邀请方 B】。
 * 内容：谁在邀请我（inviterId/inviterName）——B 的客户端据此弹“XXX 想加你为好友”。
 */
public class AddUserRespPacket extends Packet {

    private String inviterId;
    private String inviterName;

    @Override
    public Byte getCommand() {
        return Command.ADD_USER_RESPONSE;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }
}
