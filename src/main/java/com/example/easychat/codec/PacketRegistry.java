package com.example.easychat.codec;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;
import com.example.easychat.packet.friend.AcceptReqPacket;
import com.example.easychat.packet.friend.AddUserReqPacket;
import com.example.easychat.packet.group.AcceptGroupReqPacket;
import com.example.easychat.packet.group.ApplyGroupReqPacket;
import com.example.easychat.packet.group.CreateGroupReqPacket;
import com.example.easychat.packet.group.GroupMessageReqPacket;
import com.example.easychat.packet.group.InviteGroupReqPacket;
import com.example.easychat.packet.group.AllowGroupReqPacket;
import com.example.easychat.packet.heartbeat.HeartbeatRequestPacket;
import com.example.easychat.packet.login.LoginRequestPacket;
import com.example.easychat.packet.message.MessageReqPacket;
import com.example.easychat.packet.register.RegisterRequestPacket;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令 -> 数据包类 的注册表。
 *
 * 为什么需要它：解码器读出一个字节的指令后，必须知道"该把它反序列化成哪个类"。
 * 这个映射就是协议的"消息类型定义"——每实现一种新消息，就往这里登记一行
 * （M1 登录；M2 注册+心跳；M4 好友；M5 群组+消息）。
 *
 * 面试点：这是"用 Map 做分发"的经典场景，替代一长串 if-else，
 * 新增消息类型时遵守"开闭原则"——只加数据，不改结构。
 */
public final class PacketRegistry {

    private static final Map<Byte, Class<? extends Packet>> PACKET_TYPES = new HashMap<>();

    static {
        PACKET_TYPES.put(Command.LOGIN_REQUEST, LoginRequestPacket.class);
        PACKET_TYPES.put(Command.REGISTER_REQUEST, RegisterRequestPacket.class);
        PACKET_TYPES.put(Command.HEARTBEAT_REQUEST, HeartbeatRequestPacket.class);
        PACKET_TYPES.put(Command.ADD_USER_REQUEST, AddUserReqPacket.class);
        PACKET_TYPES.put(Command.ACCEPT_FRIEND_REQUEST, AcceptReqPacket.class);
        PACKET_TYPES.put(Command.CREATE_GROUP_REQUEST, CreateGroupReqPacket.class);
        PACKET_TYPES.put(Command.INVITE_GROUP_REQUEST, InviteGroupReqPacket.class);
        PACKET_TYPES.put(Command.ACCEPT_GROUP_REQUEST, AcceptGroupReqPacket.class);
        PACKET_TYPES.put(Command.MESSAGE_REQUEST, MessageReqPacket.class);
        PACKET_TYPES.put(Command.GROUP_MESSAGE_REQUEST, GroupMessageReqPacket.class);
        PACKET_TYPES.put(Command.APPLY_GROUP_REQUEST, ApplyGroupReqPacket.class);
        PACKET_TYPES.put(Command.ALLOW_GROUP_REQUEST, AllowGroupReqPacket.class);
        // M6 将加入 REST 层（HTTP，不走此注册表）
    }

    public static Class<? extends Packet> get(Byte command) {
        return PACKET_TYPES.get(command);
    }

    private PacketRegistry() {
    }
}
