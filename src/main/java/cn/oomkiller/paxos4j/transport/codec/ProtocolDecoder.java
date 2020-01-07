package cn.oomkiller.paxos4j.transport.codec;

import cn.oomkiller.paxos4j.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        log.info("Message decoder");
        Message message = new Message();
        message.decode(buf);

        out.add(message);
    }
}


