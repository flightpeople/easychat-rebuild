package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 入群审批响应（指令 33）：推送给【申请人】。
 * allow=true 表示群主同意了（可以进群）；false 表示被拒。
 */
public class AllowGroupRespPacket extends Packet {

    private String groupId;
    private boolean allow;

    @Override
    public Byte getCommand() {
        return Command.ALLOW_GROUP_RESPONSE;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean allow) {
        this.allow = allow;
    }
}
