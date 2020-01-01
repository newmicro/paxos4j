package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpServerHandler extends SimpleChannelInboundHandler<Message> {
  private final MessageHandler handler;

  public TcpServerHandler(MessageHandler handler) {
    this.handler = handler;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    handler.onReceiveMessage(msg);
  }
}
