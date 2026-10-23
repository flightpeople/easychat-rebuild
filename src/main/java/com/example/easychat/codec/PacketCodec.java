package com.example.easychat.codec;

import com.example.easychat.protocol.MagicNumber;
import com.example.easychat.protocol.Packet;
import com.example.easychat.protocol.SerializerAlgorithm;
import com.example.easychat.serializer.JsonSerializer;
import com.example.easychat.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * 协议编解码器：Packet 对象 <-> 网络字节。
 *
 * 继承 Netty 的 MessageToMessageCodec<ByteBuf, Packet>：
 * - 入站（网络 → 程序）：ByteBuf 解码成 Packet，交给后续 Handler；
 * - 出站（程序 → 网络）：Handler 写出 Packet 时，编码成 ByteBuf。
 * 一个类搞定两个方向，Netty 会按消息类型自动走对应分支。
 *
 * @ChannelHandler.Sharable：编解码器无状态（只有一个 Jackson 实例且线程安全），
 * 所有连接共享同一个 INSTANCE。Netty 的安全契约：共享的 Handler 实例必须显式
 * 标注 @Sharable，否则加入 pipeline 时报 ChannelPipelineException
 * （漏标这个注解是 Netty 新手的经典错误，本次开发实测踩到）。
 *
 * 帧格式（11 字节头 + 变长体）：
 *   魔数 4B | 版本 1B | 序列化算法 1B | 指令 1B | 长度 4B | JSON 体 NB
 *
 * 面试点：为什么要"长度字段"？——TCP 是字节流没有边界，
 * 接收方必须知道"这一包到哪个字节结束"，否则粘包/半包无法拆分。
 */
@io.netty.channel.ChannelHandler.Sharable
public class PacketCodec extends MessageToMessageCodec<ByteBuf, Packet> {

    public static final PacketCodec INSTANCE = new PacketCodec();

    /** 协议头长度：4 + 1 + 1 + 1 + 4 */
    private static final int HEADER_LENGTH = 11;

    private final Serializer serializer = new JsonSerializer();

    // ---------- 出站：Packet -> ByteBuf ----------
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {
        byte[] body = serializer.serialize(packet);
        ByteBuf buf = ctx.alloc().buffer(HEADER_LENGTH + body.length);
        buf.writeInt(MagicNumber.MAGIC);                              // 魔数
        buf.writeByte(Packet.VERSION);                                // 版本
        buf.writeByte(SerializerAlgorithm.JSON);                      // 序列化算法
        buf.writeByte(packet.getCommand());                           // 指令
        buf.writeInt(body.length);                                    // 体长度
        buf.writeBytes(body);                                         // 体
        out.add(buf);
    }

    // ---------- 入站：ByteBuf -> Packet ----------
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 此时魔数已被 Spliter 校验并跳过，直接从版本号读起
        in.skipBytes(4);                                              // 跳过魔数
        in.skipBytes(1);                                              // 跳过版本
        in.skipBytes(1);                                              // 跳过序列化算法（M1 只实现 JSON）
        byte command = in.readByte();                                 // 指令
        int length = in.readInt();                                    // 体长度
        byte[] body = new byte[length];
        in.readBytes(body);                                           // 体

        Class<? extends Packet> clazz = PacketRegistry.get(command);
        if (clazz == null) {
            // 未登记的指令：记录但不当断连接（M1 宽容处理，M2 起可改为关闭通道）
            System.err.println("[PacketCodec] 未注册的指令: " + command);
            return;
        }
        out.add(serializer.deserialize(clazz, body));
    }
}
