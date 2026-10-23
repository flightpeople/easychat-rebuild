package com.example.easychat.server;

import com.example.easychat.service.FriendService;
import com.example.easychat.service.GroupService;
import com.example.easychat.service.MessageService;
import com.example.easychat.service.OnlineStatusService;
import com.example.easychat.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Netty 启动器（M6a）。
 *
 * 为什么用 CommandLineRunner 而不是 @PostConstruct：
 * CommandLineRunner 在所有 Bean 初始化完成、应用完全就绪后才执行，
 * 且能拿到构造注入的依赖——Netty 的 Handler 需要它们。
 *
 * 注意：run() 里阻塞等待服务端通道关闭，应用会一直存活；
 * Ctrl+C 时 Spring 的关闭钩子 + Netty 的 shutdownGracefully 依次生效。
 */
@Component
public class NettyStarter implements CommandLineRunner {

    private final UserService userService;
    private final FriendService friendService;
    private final GroupService groupService;
    private final MessageService messageService;
    private final OnlineStatusService onlineStatusService;

    public NettyStarter(UserService userService, FriendService friendService,
                        GroupService groupService, MessageService messageService,
                        OnlineStatusService onlineStatusService) {
        this.userService = userService;
        this.friendService = friendService;
        this.groupService = groupService;
        this.messageService = messageService;
        this.onlineStatusService = onlineStatusService;
    }

    @Override
    public void run(String... args) throws Exception {
        new NettyServer(userService, friendService, groupService, messageService, onlineStatusService).start();
    }
}
