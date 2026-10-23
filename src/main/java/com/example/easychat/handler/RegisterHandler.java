package com.example.easychat.handler;

import com.example.easychat.packet.register.RegisterRequestPacket;
import com.example.easychat.packet.register.RegisterResponsePacket;
import com.example.easychat.service.UserService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 注册处理器（指令 21）。
 *
 * 流程：查重 -> 建用户（密码 BCrypt 哈希后入库）-> 回成败。
 * 用户名重复时回 success=false + reason——与原版的“已存在则失败”语义对齐。
 *
 * M3 起 UserService 由 Spring 注入（构造参数）——Handler 仍是 @Sharable，
 * 但依赖不再是自己 new，而是外部传入（IoC 的起点：谁使用谁被注入）。
 */
@io.netty.channel.ChannelHandler.Sharable
public class RegisterHandler extends SimpleChannelInboundHandler<RegisterRequestPacket> {

    private final UserService userService;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RegisterRequestPacket packet) {
        System.out.println("[RegisterHandler] 注册请求: userName=" + packet.getUserName());

        RegisterResponsePacket resp = new RegisterResponsePacket();

        if (packet.getUserName() == null || packet.getUserName().trim().isEmpty()) {
            resp.setSuccess(false);
            resp.setReason("用户名不能为空");
        } else if (userService.existsByName(packet.getUserName())) {
            resp.setSuccess(false);
            resp.setReason("用户名已存在");
            System.out.println("[RegisterHandler] 注册失败(重名): " + packet.getUserName());
        } else {
            userService.register(packet.getUserName(), packet.getPassword(), packet.getNickName());
            resp.setSuccess(true);
        }

        ctx.writeAndFlush(resp);
    }
}
