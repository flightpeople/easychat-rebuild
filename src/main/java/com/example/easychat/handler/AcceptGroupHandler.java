package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.packet.group.AcceptGroupReqPacket;
import com.example.easychat.packet.group.AcceptGroupRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 接受入群处理器（指令 17）：同意则入群，双方都收 18。
 */
@io.netty.channel.ChannelHandler.Sharable
public class AcceptGroupHandler extends SimpleChannelInboundHandler<AcceptGroupReqPacket> {

    private final GroupService groupService;

    public AcceptGroupHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AcceptGroupReqPacket packet) {
        String acceptUserId = SessionUtil.getUserId(ctx.channel());
        String groupId = packet.getGroupId();
        String inviterId = packet.getInviterId();
        System.out.println("[AcceptGroupHandler] " + acceptUserId + " 回复入群邀请: groupId=" + groupId
                + ", " + (packet.isAccept() ? "同意" : "拒绝"));

        AcceptGroupRespPacket resp = new AcceptGroupRespPacket();
        resp.setAcceptUser(acceptUserId);
        resp.setGroupId(groupId);

        Group group = groupService.getById(groupId);
        if (group == null) {
            resp.setSuccess(false);
            ctx.writeAndFlush(resp);
            return;
        }

        if (packet.isAccept()) {
            groupService.addMember(groupId, acceptUserId);
            resp.setSuccess(true);
        } else {
            resp.setSuccess(false);
        }

        // 接受方回执
        ctx.writeAndFlush(resp);
        // 邀请方在线则同步（群里多了一个人，邀请方客户端可刷新成员列表）
        Channel inviterChannel = SessionUtil.getChannelByUserId(inviterId);
        if (inviterChannel != null) {
            inviterChannel.writeAndFlush(resp);
        }
    }
}
