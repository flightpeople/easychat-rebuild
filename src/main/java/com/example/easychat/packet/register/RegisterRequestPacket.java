package com.example.easychat.packet.register;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 注册请求（指令 21）。
 *
 * login 字段是原版协议就有的：注册后是否直接走登录流程。
 * 复刻版的处理与原版一致——注册响应只回成败，由客户端再发登录指令，
 * 保持协议行为不变（兼容性优先）。
 */
public class RegisterRequestPacket extends Packet {

    private String userName;
    private String password;
    private String nickName;
    private boolean login;

    @Override
    public Byte getCommand() {
        return Command.REGISTER_REQUEST;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
