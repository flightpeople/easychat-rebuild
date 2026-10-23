package com.example.easychat.server;

import com.example.easychat.codec.PacketCodec;
import com.example.easychat.codec.Spliter;
import com.example.easychat.handler.AcceptGroupHandler;
import com.example.easychat.handler.AcceptReqHandler;
import com.example.easychat.handler.AddUserHandler;
import com.example.easychat.handler.AllowGroupHandler;
import com.example.easychat.handler.ApplyGroupHandler;
import com.example.easychat.handler.CreateGroupHandler;
import com.example.easychat.handler.GroupMessageHandler;
import com.example.easychat.handler.HeartbeatHandler;
import com.example.easychat.handler.InviteGroupHandler;
import com.example.easychat.handler.LoginHandler;
import com.example.easychat.handler.MessageHandler;
import com.example.easychat.handler.RegisterHandler;
import com.example.easychat.service.FriendService;
import com.example.easychat.service.GroupService;
import com.example.easychat.service.MessageService;
import com.example.easychat.service.OnlineStatusService;
import com.example.easychat.service.UserService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty 服务端（M3：实例化改造，由 Spring 托管依赖）。
 *
 * M1/M2 时代 Handler 是静态 INSTANCE、内部自己 new UserService；
 * M3 起 UserService 是 Spring Bean（背后是 MySQL），
 * 所以 NettyServer 改为“构造传入 UserService，再 new 出各 Handler”。
 * Handler 仍是 @Sharable 无状态单实例——构造注入的依赖只读不写，线程安全。
 *
 * 线程模型（面试高频题）：
 * - bossGroup：1 个线程，只负责 accept 新连接；
 * - workerGroup：CPU核数*2 个线程，负责所有 IO 读写和业务。
 */
public class NettyServer {

    /** TCP 端口（避开原版 18000） */
    public static final int PORT = 18001;

    private final UserService userService;
    private final FriendService friendService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final OnlineStatusService onlineStatusService;

    public NettyServer(UserService userService, FriendService friendService,
                       GroupService groupService, MessageService messageService,
                       OnlineStatusService onlineStatusService) {
        this.userService = userService;
        this.friendService = friendService;
        this.groupService = groupService;
        this.messageService = messageService;
        this.onlineStatusService = onlineStatusService;
    }

    public void start() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new Spliter());
                            ch.pipeline().addLast(PacketCodec.INSTANCE);
                            ch.pipeline().addLast(new RegisterHandler(userService));
                            ch.pipeline().addLast(new LoginHandler(userService, onlineStatusService));
                            ch.pipeline().addLast(new AddUserHandler(userService, friendService));
                            ch.pipeline().addLast(new AcceptReqHandler(friendService));
                            ch.pipeline().addLast(new CreateGroupHandler(groupService));
                            ch.pipeline().addLast(new InviteGroupHandler(groupService));
                            ch.pipeline().addLast(new AcceptGroupHandler(groupService));
                            ch.pipeline().addLast(new GroupMessageHandler(groupService, messageService, userService));
                            ch.pipeline().addLast(new ApplyGroupHandler(groupService, userService));
                            ch.pipeline().addLast(new AllowGroupHandler(groupService));
                            ch.pipeline().addLast(new MessageHandler(userService, friendService, messageService));
                            ch.pipeline().addLast(HeartbeatHandler.INSTANCE);
                        }
                    });

            Channel channel = bootstrap.bind(PORT).sync().channel();
            System.out.println("=========================================");
            System.out.println(" EasyChat-Rebuild started (M6a)");
            System.out.println(" TCP  port: " + PORT + " (real-time channel)");
            System.out.println(" REST port: 19005 (query channel, Spring MVC)");
            System.out.println(" features: full chat + REST queries + Redis online status");
            System.out.println("=========================================");

            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
