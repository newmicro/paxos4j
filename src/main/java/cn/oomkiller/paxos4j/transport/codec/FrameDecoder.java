package cn.oomkiller.paxos4j.transport.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FrameDecoder extends LengthFieldBasedFrameDecoder {
    public FrameDecoder() {
        super(Integer.MAX_VALUE, 0, 2, 0, 2);
    }
}
