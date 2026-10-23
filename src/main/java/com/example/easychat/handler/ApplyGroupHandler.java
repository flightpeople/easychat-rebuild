package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.model.User;
import com.example.easychat.packet.group.AllowGroupReqPacket;
import com.example.easychat.packet.group.AllowGroupRespPacket;
import com.example.easychat.packet.group.ApplyGroupReqPacket;
import com.example.easychat.packet.group.ApplyGroupRespPacket;
import com.example.easychat.packet.group.ApplyGroupSelfRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.service.UserService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 入群申请处理器（指令 29）：申请人 → 群主。
 *
 * 和“邀请入群”(12) 的区别：邀请是群成员主动拉人；申请是外人主动敲门。
 * 两条路最终都汇到同一个动作：对方同意 → addMember。
 */
@io.netty.channel.ChannelHandler.Sharable
public class ApplyGroupHandler extends SimpleChannelInboundHandler<ApplyGroupReqPacket> {

    private final GroupService groupService;
    private final UserService userService;

    public ApplyGroupHandler(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ApplyGroupReqPacket packet) {
        String applyUserId = SessionUtil.getUserId(ctx.channel());
        String groupId = packet.getGroupId();
        System.out.println("[ApplyGroupHandler] " + applyUserId + " 申请入群: " + groupId);

        ApplyGroupSelfRespPacket selfResp = new ApplyGroupSelfRespPacket();
        selfResp.setGroupId(groupId);

        Group group = groupService.getById(groupId);
        if (group == null) {
            selfResp.setSuccess(false);
            selfResp.setReason("群不存在");
            ctx.writeAndFlush(selfResp);
            return;
        }
        if (groupService.isMember(groupId, applyUserId)) {
            selfResp.setSuccess(false);
            selfResp.setReason("你已是群成员");
            ctx.writeAndFlush(selfResp);
            return;
        }

        // 找群主，在线才送得过去
        Channel ownerChannel = SessionUtil.getChannelByUserId(group.getMainUserId());
        if (ownerChannel == null) {
            selfResp.setSuccess(false);
            selfResp.setReason("群主不在线");
            ctx.writeAndFlush(selfResp);
            return;
        }

        User applyUser = userService.getById(applyUserId);
        ApplyGroupRespPacket resp = new ApplyGroupRespPacket();
        resp.setApplyUserId(applyUserId);
        resp.setApplyUserName(applyUser == null ? null : applyUser.getUserName());
        resp.setGroupId(groupId);
        resp.setGroupName(group.getGroupName());
        ownerChannel.writeAndFlush(resp);

        selfResp.setSuccess(true);
        selfResp.setReason("已发送申请，等待群主审批");
        ctx.writeAndFlush(selfResp);
        System.out.println("[ApplyGroupHandler] 申请已送达群主: " + group.getMainUserId());
    }
}
