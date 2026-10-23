package com.example.easychat.packet.login;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 登录请求（指令 1）。
 *
 * 字段与原版完全一致——协议兼容要求 JSON 体字段名都对得上：
 * - userName/password：账号密码；
 * - sendCacheMessage：登录后是否推送离线期间缓存的消息；
 * - token：断线重连时可以带旧 token 恢复会话（M2 实现）。
 */
public class LoginRequestPacket extends Packet {

    private String userName;
    private String password;
    private boolean sendCacheMessage = true;
    private String token;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_REQUEST;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSendCacheMessage() {
        return sendCacheMessage;
    }

    public void setSendCacheMessage(boolean sendCacheMessage) {
        this.sendCacheMessage = sendCacheMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
