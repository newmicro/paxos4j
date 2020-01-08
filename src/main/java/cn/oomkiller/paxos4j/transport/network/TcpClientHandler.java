package cn.oomkiller.paxos4j.transport.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class TcpClientHandler extends ChannelInboundHandlerAdapter {
  private final ConcurrentMap<SocketAddress, Channel> clientMap;

  public TcpClientHandler(ConcurrentMap<SocketAddress, Channel> clientMap) {
    this.clientMap = clientMap;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    SocketAddress remoteAddress = ctx.channel().remoteAddress();
    clientMap.remove(remoteAddress);
    // Close the connection when an exception is raised.
    log.warn("Close connection with " + remoteAddress + " because of: " + cause);
    ctx.close();
  }
}
