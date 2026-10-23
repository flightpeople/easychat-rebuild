package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

import java.util.List;

/**
 * 邀请入群自响应（指令 14）：回给【邀请人自己】。
 * 批量邀请的结果汇总：谁送达了、谁失败了（不在线/已是成员）——
 * 批量操作必须给聚合结果，否则客户端不知道到底谁收到了。
 */
public class InviteGroupSelfRespPacket extends Packet {

    private List<String> successUser;
    private List<String> failedUser;
    private String groupName;
    private String groupId;

    @Override
    public Byte getCommand() {
        return Command.INVITE_GROUP_SELF_RESPONSE;
    }

    public List<String> getSuccessUser() {
        return successUser;
    }

    public void setSuccessUser(List<String> successUser) {
        this.successUser = successUser;
    }

    public List<String> getFailedUser() {
        return failedUser;
    }

    public void setFailedUser(List<String> failedUser) {
        this.failedUser = failedUser;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
