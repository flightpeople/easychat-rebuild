package com.example.easychat.handler;

import com.example.easychat.model.User;
import com.example.easychat.packet.message.MessageReqPacket;
import com.example.easychat.packet.message.MessageRespPacket;
import com.example.easychat.packet.message.MessageSelfRespPacket;
import com.example.easychat.service.FriendService;
import com.example.easychat.service.MessageService;
import com.example.easychat.service.UserService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 单聊消息处理器（指令 5）——IM 的心脏，M4 转发能力的复用。
 *
 * 流程：先落库（不丢）→ 校验好友 → 对方在线：推 6；
 * 非好友/不在线：回 25 自响应告知失败原因。
 *
 * 和 M4 的好友邀请相比，这次转发的是“内容”而不是“通知”——
 * 但从服务端视角，动作一模一样：findTargetChannel + writeAndFlush。
 */
@io.netty.channel.ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<MessageReqPacket> {

    private final UserService userService;
    private final FriendService friendService;
    private final MessageService messageService;

    public MessageHandler(UserService userService, FriendService friendService,
                          MessageService messageService) {
        this.userService = userService;
        this.friendService = friendService;
        this.messageService = messageService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageReqPacket packet) {
        String senderId = SessionUtil.getUserId(ctx.channel());
        String receiverId = packet.getReceiver();
        System.out.println("[MessageHandler] " + senderId + " -> " + receiverId
                + ": " + packet.getMessage());

        // 1. 先落库：无论对方在不在线，消息不能丢
        messageService.save(senderId, receiverId, packet.getMessage());

        MessageSelfRespPacket selfResp = new MessageSelfRespPacket();
        selfResp.setReceiverId(receiverId);

        // 2. 校验好友关系（原版语义：非好友发消息走自响应拒绝）
        if (!friendService.isFriend(senderId, receiverId)) {
            selfResp.setSuccess(false);
            selfResp.setReason("对方不是你的好友");
            ctx.writeAndFlush(selfResp);
            return;
        }

        // 3. 对方在线才推送
        Channel target = SessionUtil.getChannelByUserId(receiverId);
        if (target == null) {
            selfResp.setSuccess(false);
            selfResp.setReason("对方不在线");
            ctx.writeAndFlush(selfResp);
            return;
        }

        // 4. 投递（注意 senderName：接收方视角直接渲染用）
        User sender = userService.getById(senderId);
        MessageRespPacket resp = new MessageRespPacket();
        resp.setSender(senderId);
        resp.setSenderName(sender == null ? null : sender.getUserName());
        resp.setMessage(packet.getMessage());
        resp.setMessageType(packet.getMessageType());
        target.writeAndFlush(resp);
        // 成功投递不给发送方回执——对方的回复本身就是送达证明
        System.out.println("[MessageHandler] 消息已投递: " + senderId + " -> " + receiverId);
    }
}
