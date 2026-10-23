package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.packet.group.AllowGroupReqPacket;
import com.example.easychat.packet.group.AllowGroupRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 入群审批处理器（指令 32）：群主同意/拒绝。
 *
 * 权限校验是重点：只有群主能审批——生产中这里是最常见的越权漏洞点
 * （“我不是群主为什么能拉人/踢人”）。本 Handler 显式校验 main_user_id。
 */
@io.netty.channel.ChannelHandler.Sharable
public class AllowGroupHandler extends SimpleChannelInboundHandler<AllowGroupReqPacket> {

    private final GroupService groupService;

    public AllowGroupHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AllowGroupReqPacket packet) {
        String operatorId = SessionUtil.getUserId(ctx.channel());
        String groupId = packet.getGroupId();
        String applicantId = packet.getUserId();
        System.out.println("[AllowGroupHandler] 群主 " + operatorId + " 审批: user=" + applicantId
                + ", allow=" + packet.isAllow());

        Group group = groupService.getById(groupId);
        // 权限校验：必须是群主
        if (group == null || !group.getMainUserId().equals(operatorId)) {
            System.out.println("[AllowGroupHandler] 非群主审批，拒绝");
            return;
        }

        if (packet.isAllow()) {
            groupService.addMember(groupId, applicantId);
        }

        // 通知申请人（在线才推）
        Channel applicantChannel = SessionUtil.getChannelByUserId(applicantId);
        if (applicantChannel != null) {
            AllowGroupRespPacket resp = new AllowGroupRespPacket();
            resp.setGroupId(groupId);
            resp.setAllow(packet.isAllow());
            applicantChannel.writeAndFlush(resp);
        }
        System.out.println("[AllowGroupHandler] 审批完成: " + (packet.isAllow() ? "同意" : "拒绝"));
    }
}
