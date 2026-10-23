package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 申请入群自响应（指令 31）：回给【申请人自己】。
 * 申请没送到群主那里时（群不存在/群主不在线/已是成员），申请人要得到明确反馈。
 */
public class ApplyGroupSelfRespPacket extends Packet {

    private String groupId;
    private String reason;
    private boolean success;

    @Override
    public Byte getCommand() {
        return Command.APPLY_GROUP_SELF_RESPONSE;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
