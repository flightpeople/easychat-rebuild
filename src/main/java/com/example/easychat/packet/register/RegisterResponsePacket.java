package com.example.easychat.packet.register;

import com.example.easychat.protocol.Command;
import com.example.easychat.protocol.Packet;

/**
 * 注册响应（指令 22）。
 *
 * 与原版实测行为一致：只回 success + reason，不带 token——
 * 注册完由客户端自行发登录指令。
 */
public class RegisterResponsePacket extends Packet {

    private boolean success;
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.REGISTER_RESPONSE;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
