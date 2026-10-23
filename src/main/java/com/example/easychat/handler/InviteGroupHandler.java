package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.packet.group.InviteGroupReqPacket;
import com.example.easychat.packet.group.InviteGroupRespPacket;
import com.example.easychat.packet.group.InviteGroupSelfRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 邀请入群处理器（指令 12）：批量拉人。
 *
 * 流程：校验邀请人是群成员 → 遍历被邀请人：在线且未入群的推 13，
 * 其余进 failedUser → 最后给邀请人回 14 汇总（successUser/failedUser）。
 *
 * 批量操作的汇报模式：逐个推送 + 聚合回执。原版文档明确要求
 * “服务端遍历发送邀请入群消息给被邀请的用户，并发送自收消息给邀请方”。
 */
@io.netty.channel.ChannelHandler.Sharable
public class InviteGroupHandler extends SimpleChannelInboundHandler<InviteGroupReqPacket> {

    private final GroupService groupService;

    public InviteGroupHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InviteGroupReqPacket packet) {
        String inviterId = SessionUtil.getUserId(ctx.channel());
        String groupId = packet.getGroupId();
        System.out.println("[InviteGroupHandler] " + inviterId + " 邀请入群: groupId=" + groupId);

        Group group = groupService.getById(groupId);
        InviteGroupSelfRespPacket selfResp = new InviteGroupSelfRespPacket();
        selfResp.setGroupId(groupId);
        List<String> successUser = new ArrayList<>();
        List<String> failedUser = new ArrayList<>();

        if (group == null) {
            failedUser.addAll(packet.getUsers() == null ? List.of() : packet.getUsers());
        } else if (!groupService.isMember(groupId, inviterId)) {
            // 非群成员不能拉人
            failedUser.addAll(packet.getUsers() == null ? List.of() : packet.getUsers());
        } else {
            selfResp.setGroupName(group.getGroupName());
            if (packet.getUsers() != null) {
                for (String userId : packet.getUsers()) {
                    if (groupService.isMember(groupId, userId)) {
                        failedUser.add(userId);  // 已在群里
                        continue;
                    }
                    Channel target = SessionUtil.getChannelByUserId(userId);
                    if (target == null) {
                        failedUser.add(userId);  // 不在线
                        continue;
                    }
                    InviteGroupRespPacket resp = new InviteGroupRespPacket();
                    resp.setGroupName(group.getGroupName());
                    resp.setGroupId(groupId);
                    resp.setInviteId(inviterId);
                    target.writeAndFlush(resp);
                    successUser.add(userId);
                }
            }
        }

        selfResp.setSuccessUser(successUser);
        selfResp.setFailedUser(failedUser);
        ctx.writeAndFlush(selfResp);
        System.out.println("[InviteGroupHandler] 送达 " + successUser.size()
                + " 人, 失败 " + failedUser.size() + " 人");
    }
}
