package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.packet.group.CreateGroupReqPacket;
import com.example.easychat.packet.group.CreateGroupRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 创建群处理器（指令 10）：建群 + 群主入群。
 * users 字段（建群时想拉的人）只接收不使用——与原版语义一致：
 * 拉任何人（包括建群时带的）都要走“邀请-接受”闭环，群主直接进。
 */
@io.netty.channel.ChannelHandler.Sharable
public class CreateGroupHandler extends SimpleChannelInboundHandler<CreateGroupReqPacket> {

    private final GroupService groupService;

    public CreateGroupHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CreateGroupReqPacket packet) {
        String ownerId = SessionUtil.getUserId(ctx.channel());
        System.out.println("[CreateGroupHandler] " + ownerId + " 建群: " + packet.getGroupName());

        CreateGroupRespPacket resp = new CreateGroupRespPacket();
        resp.setGroupName(packet.getGroupName());

        if (packet.getGroupName() == null || packet.getGroupName().trim().isEmpty()) {
            resp.setSuccess(false);
            resp.setReason("群名不能为空");
        } else {
            Group group = groupService.createGroup(packet.getGroupName(), ownerId);
            resp.setSuccess(true);
            resp.setGroupId(group.getGroupId());
        }
        ctx.writeAndFlush(resp);
    }
}
