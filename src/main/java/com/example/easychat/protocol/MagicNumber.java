package com.example.easychat.protocol;

/**
 * 协议魔数。
 *
 * 作用：快速判定一个数据包是否是"自己人"发的。
 * 1. 防错包：客户端连错端口/收到非协议流量时，第一帧就能发现不对劲；
 * 2. 防半包残留：拆包器（Spliter）在读取长度字段前先校验魔数，
 *    魔数不对直接断开，不会把垃圾数据当成正常包解析。
 */
public final class MagicNumber {

    /** 0x12345678，与原版 EasyChat 协议保持一致（协议兼容的关键） */
    public static final int MAGIC = 0x12345678;

    private MagicNumber() {
    }
}
