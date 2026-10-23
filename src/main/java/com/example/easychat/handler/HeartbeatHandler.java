package com.example.easychat.handler;

import com.example.easychat.packet.heartbeat.HeartbeatRequestPacket;
import com.example.easychat.packet.heartbeat.HeartbeatResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳处理器（指令 26）+ 空闲连接处置。
 *
 * 两件事：
 * 1. 收到心跳 -> 原样回一个空响应（27）。为什么服务端也要回？
 *    确认是双向通路——能收能发才算真活着（类比：你喊“在吗”，
 *    对方回“在”，才是真的在；只收到不回，可能是单向故障）。
 * 2. 空闲超时事件：IdleStateHandler（在 pipeline 里，15 秒读空闲）
 *    触发 IdleStateEvent 后顺着流水线传到这——超过 15 秒没收到
 *    客户端任何数据（包括心跳），判定掉线，主动断开并清理会话。
 *    没有这个机制，拔网线/崩溃的客户端会留下“幽灵连接”永远占资源。
 */
@io.netty.channel.ChannelHandler.Sharable
public class HeartbeatHandler extends SimpleChannelInboundHandler<HeartbeatRequestPacket> {

    public static final HeartbeatHandler INSTANCE = new HeartbeatHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartbeatRequestPacket packet) {
        ctx.writeAndFlush(new HeartbeatResponsePacket());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            System.out.println("[HeartbeatHandler] 读空闲超时，判定掉线并断开: "
                    + ctx.channel().remoteAddress());
            ctx.close();  // close 会触发 channelInactive -> LoginHandler 清理会话
        } else {
            // 其他事件继续往下传（Netty 事件传播的规矩）
            ctx.fireUserEventTriggered(evt);
        }
    }
}
