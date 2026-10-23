package com.example.easychat.packet.heartbeat;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 心跳请求（指令 26）。
 *
 * 空包——不带任何业务字段。心跳的唯一目的就是“告诉服务端我还活着”，
 * 顺便让服务端重置“读空闲计时器”。这叫 keep-alive（保活）。
 */
public class HeartbeatRequestPacket extends Packet {

    @Override
    public Byte getCommand() {
        return Command.HEARTBEAT_REQUEST;
    }
}
