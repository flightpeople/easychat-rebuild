package com.example.easychat.codec;

import com.example.easychat.protocol.MagicNumber;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 拆包器：解决 TCP 粘包/半包，并做魔数校验。
 *
 * 实现说明（与原版的差异，面试可讲）：
 * 原版覆盖的是 decode(ctx, ByteBuf) 这个"取一帧"的重载——
 * 注意 LengthFieldBasedFrameDecoder 的三参 decode(ctx, in, out) 是 final 不能覆盖，
 * 而"取一帧"的 decode(ctx, in) 才是留给子类的扩展点。这是读 Netty 源码
 * 才能发现的细节（我第一版就覆盖错了被编译器拦住）。
 *
 * 相比原版多做的一处健壮性修复：原版直接 in.getInt(readerIndex()) 读魔数，
 * 若首个 TCP 分片不足 4 字节会抛 IndexOutOfBoundsException；
 * 这里先判断 readableBytes()，不够就返回 null 等下一片数据（Netty 累积机制）。
 */
public class Spliter extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_FIELD_OFFSET = 7;
    private static final int LENGTH_FIELD_LENGTH = 4;

    public Spliter() {
        // maxFrameLength=Integer.MAX_VALUE, 长度字段在偏移 7 处、占 4 字节
        super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 数据不足 4 字节：读不了魔数，等累积（原版缺这个判断，是个潜在崩溃点）
        if (in.readableBytes() < 4) {
            return null;
        }
        // peek 魔数（getInt 不移动读指针，区别于 readInt）
        if (in.getInt(in.readerIndex()) != MagicNumber.MAGIC) {
            System.err.println("[Spliter] 魔数校验失败(0x" + Integer.toHexString(in.getInt(in.readerIndex()))
                    + ")，断开连接: " + ctx.channel().remoteAddress());
            ctx.channel().close();
            return null;
        }
        // 魔数正确，交给父类按长度字段切出一帧
        return super.decode(ctx, in);
    }
}
