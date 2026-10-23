package com.example.easychat.packet.login;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 登录响应（指令 2）。
 *
 * 字段与原版一致：success 标记成败，成功时带 token/userId，
 * 失败时 reason 说明原因。这个 token 后面有两个用途：
 * 1. REST 接口的鉴权头（M6）；
 * 2. 断线重连时凭旧 token 找回会话（M2）。
 */
public class LoginResponsePacket extends Packet {

    private boolean success;
    private String token;
    private String userId;
    private String userName;
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_RESPONSE;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
