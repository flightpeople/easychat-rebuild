package com.example.easychat.packet.heartbeat;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 心跳响应（指令 27）。
 *
 * 与原版一致：空响应。客户端收到指令 27 就知道连接健康，
 * 不需要任何数据。空包也是包——协议帧的“信封”部分照样完整。
 */
public class HeartbeatResponsePacket extends Packet {

    @Override
    public Byte getCommand() {
        return Command.HEARTBEAT_RESPONSE;
    }
}
