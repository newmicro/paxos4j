package cn.oomkiller.paxos4j.transport.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;

@Slf4j
public class TcpClientHandler extends ChannelInboundHandlerAdapter {
  private final Map<SocketAddress, Channel> clientMap;

  public TcpClientHandler(Map<SocketAddress, Channel> clientMap) {
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
