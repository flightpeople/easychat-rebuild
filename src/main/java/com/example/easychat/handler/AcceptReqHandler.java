package com.example.easychat.handler;

import com.example.easychat.model.User;
import com.example.easychat.packet.friend.AcceptReqPacket;
import com.example.easychat.packet.friend.AcceptRespPacket;
import com.example.easychat.service.FriendService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 接受好友请求处理器（指令 19）——M4 闭环的另一半。
 *
 * 流程：B 回复（同意/拒绝）→ 同意则落库（双向两条）→ 给【双方】都推 20。
 *
 * 拒绝时也回 20（success=false）：让邀请方知道被拒了，
 * 客户端可以把“等待验证”变成“对方拒绝了你”——状态闭环，不悬置。
 */
@io.netty.channel.ChannelHandler.Sharable
public class AcceptReqHandler extends SimpleChannelInboundHandler<AcceptReqPacket> {

    private final FriendService friendService;

    public AcceptReqHandler(FriendService friendService) {
        this.friendService = friendService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AcceptReqPacket packet) {
        String invitedId = SessionUtil.getUserId(ctx.channel());   // 接受方（B）
        String inviterId = packet.getReceiver();                    // 邀请方（A）
        boolean accept = packet.isAccept();
        System.out.println("[AcceptReqHandler] " + invitedId + " 回复 " + inviterId
                + " 的好友请求: " + (accept ? "同意" : "拒绝"));

        AcceptRespPacket resp = new AcceptRespPacket();
        resp.setInviterId(inviterId);
        resp.setInvitedId(invitedId);
        User invited = friendService.getUser(invitedId);
        resp.setInvitedName(invited == null ? null : invited.getUserName());

        if (accept) {
            // 幂等：已是好友（比如邀请方之前加过）也按成功处理
            if (!friendService.isFriend(inviterId, invitedId)) {
                friendService.createFriendship(inviterId, invitedId);
            }
            resp.setSuccess(true);
        } else {
            resp.setSuccess(false);
        }

        // 给接受方回执
        ctx.writeAndFlush(resp);
        // 邀请方在线则同步通知（原版文档：接受响应消息发送到邀请方）
        Channel inviterChannel = SessionUtil.getChannelByUserId(inviterId);
        if (inviterChannel != null) {
            inviterChannel.writeAndFlush(resp);
        }
        System.out.println("[AcceptReqHandler] 好友关系已建立: " + inviterId + " <-> " + invitedId);
    }
}
