package com.example.easychat.handler;

import com.example.easychat.model.Group;
import com.example.easychat.model.User;
import com.example.easychat.packet.group.GroupMessageReqPacket;
import com.example.easychat.packet.group.GroupMessageRespPacket;
import com.example.easychat.service.GroupService;
import com.example.easychat.service.MessageService;
import com.example.easychat.service.UserService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * 群聊消息处理器（指令 15）——第一次接触“扇出”(fan-out)。
 *
 * 和单聊的本质区别：单聊是“点对点”（找到一个 Channel 写过去），
 * 群聊是“一对多”（遍历群成员，给每个在线的成员各写一份）。
 * 群成员 100 人 = 一次接收、100 次写出——这就是“扇出”，
 * 也是群消息比单聊贵的根源（大群性能问题都出在这，M7 压测会验证）。
 *
 * 落库策略：一条群消息只在 sp_message 存一条（to_user_id = groupId），
 * 不是给每个成员存一条——存储按“逻辑消息”存，投递按“物理成员”发。
 */
@io.netty.channel.ChannelHandler.Sharable
public class GroupMessageHandler extends SimpleChannelInboundHandler<GroupMessageReqPacket> {

    private final GroupService groupService;
    private final MessageService messageService;
    private final UserService userService;

    public GroupMessageHandler(GroupService groupService, MessageService messageService,
                               UserService userService) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMessageReqPacket packet) {
        String senderId = SessionUtil.getUserId(ctx.channel());
        String groupId = packet.getGroupId();
        System.out.println("[GroupMessageHandler] " + senderId + " 群消息: groupId=" + groupId);

        // 1. 先落库（一条）
        messageService.save(senderId, groupId, packet.getMessage());

        // 2. 校验：群存在 + 发送者是成员
        Group group = groupService.getById(groupId);
        if (group == null || !groupService.isMember(groupId, senderId)) {
            System.out.println("[GroupMessageHandler] 非群成员或群不存在，拒绝");
            return;
        }

        // 3. 扇出：遍历成员逐个推送（含发送者自己——微信模式：自己发的也渲染）
        User sender = userService.getById(senderId);
        List<String> memberIds = groupService.memberIds(groupId);
        int delivered = 0;
        for (String memberId : memberIds) {
            Channel memberChannel = SessionUtil.getChannelByUserId(memberId);
            if (memberChannel == null) {
                continue;  // 离线成员跳过（消息已落库，上线后可补）
            }
            GroupMessageRespPacket resp = new GroupMessageRespPacket();
            resp.setSenderId(senderId);
            resp.setSenderName(sender == null ? null : sender.getUserName());
            resp.setAvatar(sender == null ? null : sender.getAvatar());
            resp.setGroupId(groupId);
            resp.setMessage(packet.getMessage());
            resp.setMessageType(packet.getMessageType());
            memberChannel.writeAndFlush(resp);
            delivered++;
        }
        System.out.println("[GroupMessageHandler] 群消息扇出: " + delivered + "/" + memberIds.size() + " 人在线收到");
    }
}
