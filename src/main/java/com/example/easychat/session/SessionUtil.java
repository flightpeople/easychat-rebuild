package com.example.easychat.session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话表——整个系统的枢纽。
 *
 * 它回答两个问题：
 *   1. 这个 token 对应哪根网络连接？（发消息时用：查到 Channel 才能写回去）
 *   2. 这个用户当前在线吗、用哪个 token？（单会话管理用）
 *
 * 三处并发安全设计（面试重点）：
 *   1. ConcurrentHashMap：多连接并发读写；
 *   2. Channel 属性（Attribute）存 token/userId：断开时能“顺着网线”
 *      找到该清理哪些条目，不需要反查表；
 *   3. 清理用 remove(key, value) 两参版：只有“映射仍指向这个连接”才删——
 *      防止单会话替换时，旧连接的清理把新连接的登记误删掉
 *      （经典竞态：用户换手机登录，旧连接的断开回调晚于新连接的绑定）。
 */
public final class SessionUtil {

    private static final io.netty.util.AttributeKey<String> TOKEN_ATTR =
            io.netty.util.AttributeKey.newInstance("token");
    private static final io.netty.util.AttributeKey<String> USER_ID_ATTR =
            io.netty.util.AttributeKey.newInstance("userId");

    /** token -> Channel */
    private static final Map<String, Channel> TOKEN_CHANNEL = new ConcurrentHashMap<>();
    /** userId -> token（单会话：一个用户同时只有一个有效 token） */
    private static final Map<String, String> USER_TOKEN = new ConcurrentHashMap<>();

    private SessionUtil() {
    }

    /** 生成新 token（32 位无横线 UUID） */
    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 绑定会话：登录成功时调用 */
    public static void bind(String userId, String token, Channel channel) {
        TOKEN_CHANNEL.put(token, channel);
        USER_TOKEN.put(userId, token);
        channel.attr(TOKEN_ATTR).set(token);
        channel.attr(USER_ID_ATTR).set(userId);
    }

    public static Channel getChannel(String token) {
        return TOKEN_CHANNEL.get(token);
    }

    public static String getToken(String userId) {
        return USER_TOKEN.get(userId);
    }

    /**
     * 从连接反查 userId：Handler 里“当前操作者是谁”就靠它
     * （登录时绑进了 Channel 属性，断开时清理）。
     */
    public static String getUserId(Channel channel) {
        return channel.attr(USER_ID_ATTR).get();
    }

    public static Channel getChannelByUserId(String userId) {
        String token = USER_TOKEN.get(userId);
        return token == null ? null : TOKEN_CHANNEL.get(token);
    }

    public static boolean hasLoggedIn(String userId) {
        return USER_TOKEN.containsKey(userId);
    }

    /**
     * 解绑会话：连接断开时调用。
     * 两参 remove 保证“只清理仍属于自己的映射”，见类注释第 3 点。
     */
    public static void unbind(Channel channel) {
        String token = channel.attr(TOKEN_ATTR).getAndSet(null);
        String userId = channel.attr(USER_ID_ATTR).getAndSet(null);
        if (token != null) {
            TOKEN_CHANNEL.remove(token, channel);
        }
        if (userId != null) {
            USER_TOKEN.remove(userId, token);
        }
        System.out.println("[SessionUtil] 会话清理: userId=" + userId + ", token=" + token);
    }

    /** 当前在线数（演示/调试用） */
    public static int onlineCount() {
        return TOKEN_CHANNEL.size();
    }
}
