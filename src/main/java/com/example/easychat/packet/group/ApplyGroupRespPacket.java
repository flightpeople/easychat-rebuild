package com.example.easychat.packet.group;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 申请入群响应（指令 30）：推送给【群主】。
 * 带申请人姓名和群名——群主客户端弹窗“张三 申请加入 测试群”直接用。
 */
public class ApplyGroupRespPacket extends Packet {

    private String applyUserId;
    private String applyUserName;
    private String groupId;
    private String groupName;

    @Override
    public Byte getCommand() {
        return Command.APPLY_GROUP_RESPONSE;
    }

    public String getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
