package cn.oomkiller.paxos4j.transport.codec;

import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrameEncoder extends LengthFieldPrepender {
    public FrameEncoder() {
        super(2);
        log.info("Frame Encoder");
    }
}
