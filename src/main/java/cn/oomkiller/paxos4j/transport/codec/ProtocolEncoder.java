package cn.oomkiller.paxos4j.transport.codec;

import cn.oomkiller.paxos4j.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ProtocolEncoder extends MessageToMessageEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        msg.encode(buf);

        out.add(buf);
    }
}


