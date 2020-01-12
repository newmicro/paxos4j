package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
@Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<Message> {
  private final MessageHandler handler;

  public TcpServerHandler(MessageHandler handler) {
    this.handler = handler;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    handler.onReceiveMessage(msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    SocketAddress remoteAddress = ctx.channel().remoteAddress();
    // Close the connection when an exception is raised.
    log.warn("Close connection with " + remoteAddress + " because of: " + cause);
    ctx.close();
  }
}
