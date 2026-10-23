package com.example.easychat.protocol;

/**
 * 所有协议消息的基类。
 *
 * 协议头里 version 和 dateTime 是所有消息都带的公共字段：
 * - version：协议版本，现在固定 1，为将来升级预留；
 * - dateTime：消息时间（yyyyMMddHHmmss 格式），由发送方填充。
 */
public abstract class Packet {

    /** 协议版本号，写死在基类里——每个发出去的包都带 */
    public static final byte VERSION = 1;

    private String dateTime;

    /** 每个子类必须声明自己的指令 */
    public abstract Byte getCommand();

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
