package com.example.easychat.serializer;

import com.example.easychat.protocol.SerializerAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON 序列化器（算法编号 1）。
 *
 * 注意 Jackson 的两个使用细节（面试可讲）：
 * 1. ObjectMapper 线程安全，可以所有线程共用一个实例（这里是静态常量）；
 * 2. 序列化 Packet 子类时，父类字段（version/dateTime）也会被写进 JSON，
 *    反序列化时能还原——协议头里的 version 因此有了双重保障。
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.JSON;
    }

    @Override
    public <T> byte[] serialize(T object) {
        try {
            return MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败: " + object.getClass(), e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        try {
            return MAPPER.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + clazz.getName(), e);
        }
    }
}
