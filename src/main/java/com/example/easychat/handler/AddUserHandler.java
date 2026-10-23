package com.example.easychat.handler;

import com.example.easychat.model.User;
import com.example.easychat.packet.friend.AddUserReqPacket;
import com.example.easychat.packet.friend.AddUserRespPacket;
import com.example.easychat.packet.friend.AddUserSelfRespPacket;
import com.example.easychat.service.FriendService;
import com.example.easychat.service.UserService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 添加好友处理器（指令 7）——M4 的第一个“转发”处理器。
 *
 * 流程：A 发请求 → 校验 → 给 B 推 8（在线才推得到）→ 给 A 回 9 确认。
 *
 * 【转发的本质】findTargetChannel + writeAndFlush：
 * 服务端自己也是网络编程里的一个“客户端”——它拿着 B 的连接句柄，
 * 把 A 的请求“变成”B 能收到的消息。从此，两个人隔着服务器说上了话。
 *
 * 注意：这个阶段【不写好友表】——对方还没同意，只是“挂起中”。
 * 原版流程也是如此（接受时才落库）。
 */
@io.netty.channel.ChannelHandler.Sharable
public class AddUserHandler extends SimpleChannelInboundHandler<AddUserReqPacket> {

    private final UserService userService;
    private final FriendService friendService;

    public AddUserHandler(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AddUserReqPacket packet) {
        String inviterId = SessionUtil.getUserId(ctx.channel());
        String addUserId = packet.getAddUserId();
        System.out.println("[AddUserHandler] " + inviterId + " 请求添加好友: " + addUserId);

        AddUserSelfRespPacket selfResp = new AddUserSelfRespPacket();
        selfResp.setInvitedId(addUserId);

        // 1. 目标用户存在吗
        User target = userService.getById(addUserId);
        if (target == null) {
            selfResp.setSuccess(false);
            selfResp.setReason("用户不存在");
            ctx.writeAndFlush(selfResp);
            return;
        }
        // 2. 不能加自己
        if (addUserId.equals(inviterId)) {
            selfResp.setSuccess(false);
            selfResp.setReason("不能添加自己为好友");
            ctx.writeAndFlush(selfResp);
            return;
        }
        // 3. 幂等：已是好友就别重复走流程
        if (friendService.isFriend(inviterId, addUserId)) {
            selfResp.setSuccess(false);
            selfResp.setReason("对方已经是你的好友");
            ctx.writeAndFlush(selfResp);
            return;
        }
        // 4. 对方在线吗——在线才能把邀请推送过去
        Channel targetChannel = SessionUtil.getChannelByUserId(addUserId);
        if (targetChannel == null) {
            selfResp.setSuccess(false);
            selfResp.setReason("对方不在线");
            ctx.writeAndFlush(selfResp);
            return;
        }

        // 5. 转发邀请给对方（8），给自己回确认（9）
        User inviter = userService.getById(inviterId);
        AddUserRespPacket resp = new AddUserRespPacket();
        resp.setInviterId(inviterId);
        resp.setInviterName(inviter == null ? null : inviter.getUserName());
        targetChannel.writeAndFlush(resp);

        selfResp.setSuccess(true);
        selfResp.setReason("等待对方验证");
        ctx.writeAndFlush(selfResp);
        System.out.println("[AddUserHandler] 邀请已推送: " + inviterId + " -> " + addUserId);
    }
}
