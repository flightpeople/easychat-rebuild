package com.example.easychat.handler;

import com.example.easychat.model.User;
import com.example.easychat.packet.login.LoginRequestPacket;
import com.example.easychat.packet.login.LoginResponsePacket;
import com.example.easychat.service.OnlineStatusService;
import com.example.easychat.service.UserService;
import com.example.easychat.session.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 登录处理器（指令 1）——M6a 版：登录即上线（Redis 在线集合 +1）。
 *
 * 流程：查用户 -> 验密码(BCrypt) -> 单会话处理 -> 发 token -> 绑会话 -> 标记在线。
 *
 * 【单会话语义】同一账号再次登录，旧连接会被强制断开。
 * 这不是 bug 是设计：原版就是这么做的（我们测试原版时实测发现并记录过），
 * 好处是“一号一入口”，发消息时不会歧义；代价是旧设备被踢。
 * 微信的选择是允许多端，这里是课程项目的简化取舍——面试可对比讨论。
 */
@io.netty.channel.ChannelHandler.Sharable
public class LoginHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {

    private final UserService userService;
    private final OnlineStatusService onlineStatusService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginHandler(UserService userService, OnlineStatusService onlineStatusService) {
        this.userService = userService;
        this.onlineStatusService = onlineStatusService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket packet) {
        System.out.println("[LoginHandler] 登录请求: userName=" + packet.getUserName());

        LoginResponsePacket resp = new LoginResponsePacket();
        resp.setUserName(packet.getUserName());

        User user = userService.getByName(packet.getUserName());
        if (user == null) {
            resp.setSuccess(false);
            resp.setReason("用户不存在");
        } else if (!passwordEncoder.matches(packet.getPassword(), user.getPasswordHash())) {
            resp.setSuccess(false);
            resp.setReason("密码错误");
        } else {
            // 单会话：踢掉同一账号的旧连接（旧连接断开时会自动清理它的会话登记）
            if (SessionUtil.hasLoggedIn(user.getUserId())) {
                Channel oldChannel = SessionUtil.getChannelByUserId(user.getUserId());
                if (oldChannel != null && oldChannel != ctx.channel()) {
                    System.out.println("[LoginHandler] 单会话替换: 断开旧连接 " + oldChannel.remoteAddress());
                    oldChannel.close();
                }
            }
            // 发 token + 绑会话 + 标记在线（Redis）
            String token = SessionUtil.generateToken();
            SessionUtil.bind(user.getUserId(), token, ctx.channel());
            onlineStatusService.online(user.getUserId());
            resp.setSuccess(true);
            resp.setToken(token);
            resp.setUserId(user.getUserId());
            System.out.println("[LoginHandler] 登录成功: userId=" + user.getUserId()
                    + ", 当前在线 " + onlineStatusService.count() + " 人");
        }

        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 连接断开：清理会话登记 + Redis 在线状态
        String userId = SessionUtil.getUserId(ctx.channel());
        SessionUtil.unbind(ctx.channel());
        if (userId != null) {
            onlineStatusService.offline(userId);
        }
        System.out.println("[LoginHandler] 连接断开: " + ctx.channel().remoteAddress());
    }
}
