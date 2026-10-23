package com.example.easychat.protocol;

/**
 * 序列化算法表。
 *
 * 为什么协议里要带"序列化算法"字节而不是写死 JSON？
 * ——为了可扩展：将来要换 Protobuf/MessagePack，只要加一个算法编号，
 * 老客户端发算法 1、新服务端也认；新旧算法可以在一段时间内共存，
 * 这就是"协议演进"的标准做法（HTTP 的 Content-Encoding 是同一种思想）。
 */
public final class SerializerAlgorithm {

    /** 1 = JSON（当前唯一实现） */
    public static final byte JSON = 1;

    private SerializerAlgorithm() {
    }
}
