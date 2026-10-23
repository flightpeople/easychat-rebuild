package com.example.easychat.protocol;

/**
 * 指令表：一个字节，标识这个消息是干什么的。
 *
 * 设计约束（原版文档）：一个字节最多表示 128 个指令（最高位保留作区分）。
 * 取值与原版 EasyChat 完全一致——这是"协议兼容"的核心：
 * 原版客户端发什么指令，复刻服务端就认什么指令。
 *
 * 编号规律（帮助记忆，面试可讲）：
 *   - 请求/响应成对，响应 = 请求 + 1；
 *   - 带 "自响应"（SELF）的消息是服务端回给"操作发起方自己"的确认，
 *     例如加好友时，被邀请方收到 8，邀请方自己收到 9；
 *   - 登录注册用心跳区间在 1-33 内，消息类 5-25，群组类 10-33。
 */
public final class Command {

    // ---- 登录 / 注册 ----
    public static final byte LOGIN_REQUEST = 1;
    public static final byte LOGIN_RESPONSE = 2;
    public static final byte REGISTER_REQUEST = 21;
    public static final byte REGISTER_RESPONSE = 22;

    // ---- 单聊消息 ----
    public static final byte MESSAGE_REQUEST = 5;
    public static final byte MESSAGE_RESPONSE = 6;
    public static final byte MESSAGE_SELF_RESPONSE = 25;

    // ---- 好友（添加闭环：7/8/9 + 19/20）----
    public static final byte ADD_USER_REQUEST = 7;
    public static final byte ADD_USER_RESPONSE = 8;
    public static final byte ADD_USER_SELF_RESPONSE = 9;
    public static final byte ACCEPT_FRIEND_REQUEST = 19;
    public static final byte ACCEPT_FRIEND_RESPONSE = 20;

    // ---- 群组（建群/邀请：10-14；群消息：15/16；接受邀请：17/18）----
    public static final byte CREATE_GROUP_REQUEST = 10;
    public static final byte CREATE_GROUP_RESPONSE = 11;
    public static final byte INVITE_GROUP_REQUEST = 12;
    public static final byte INVITE_GROUP_RESPONSE = 13;
    public static final byte INVITE_GROUP_SELF_RESPONSE = 14;
    public static final byte GROUP_MESSAGE_REQUEST = 15;
    public static final byte GROUP_MESSAGE_RESPONSE = 16;
    public static final byte ACCEPT_GROUP_REQUEST = 17;
    public static final byte ACCEPT_GROUP_RESPONSE = 18;

    // ---- 群组（申请/审批：29-33）----
    public static final byte APPLY_GROUP_REQUEST = 29;
    public static final byte APPLY_GROUP_RESPONSE = 30;
    public static final byte APPLY_GROUP_SELF_RESPONSE = 31;
    public static final byte ALLOW_GROUP_REQUEST = 32;
    public static final byte ALLOW_GROUP_RESPONSE = 33;

    // ---- 心跳 ----
    public static final byte HEARTBEAT_REQUEST = 26;
    public static final byte HEARTBEAT_RESPONSE = 27;

    private Command() {
    }
}
