package com.example.easychat.serializer;

/**
 * 序列化器接口：对象 <-> 字节数组。
 *
 * 编解码器（PacketCodec）只依赖这个接口，不关心具体用什么算法——
 * 这就是"面向接口编程"在协议层的落地：将来加 Protobuf 实现，
 * 编解码器一行不用改。
 */
public interface Serializer {

    /** 算法编号，对应协议头里的"序列化算法"字节 */
    byte getSerializerAlgorithm();

    /** 对象序列化为字节数组 */
    <T> byte[] serialize(T object);

    /** 字节数组反序列化为对象 */
    <T> T deserialize(Class<T> clazz, byte[] bytes);
}
