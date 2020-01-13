package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.message.Message;
import cn.oomkiller.paxos4j.transport.codec.FrameEncoder;
import cn.oomkiller.paxos4j.transport.codec.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpClient extends Thread {
  private final Bootstrap bootstrap = new Bootstrap();
  private volatile boolean isEnd;
  private ConcurrentMap<SocketAddress, Channel> clientMap = new ConcurrentHashMap<>();
  private LinkedBlockingQueue<RoutedMessage> messageQueue = new LinkedBlockingQueue();

  public TcpClient() {
    super("TcpClient");
  }

  @Override
  public void run() {
    // Configure the client.
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      bootstrap
          .group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(new FrameEncoder());
                  p.addLast(new ProtocolEncoder());
                  p.addLast(new TcpClientHandler(clientMap));
                }
              });

      while (true) {
        if (isEnd) {
          return;
        }

        try {
          sendRoutedMessage(messageQueue.take());
        } catch (InterruptedException e) {
          // log.debug(String.format("Interrupted because of: %s"), e);
        }
      }
    } finally {
      // Shut down the event loop to terminate all threads.
      group.shutdownGracefully();
    }
  }

  public void addMessage(SocketAddress address, Message message) {
    messageQueue.offer(new RoutedMessage(address, message));
  }

  private void sendRoutedMessage(RoutedMessage message) {
    if (clientMap.containsKey(message.destination)) {
      Channel channel = clientMap.get(message.destination);
      if (channel.isActive()) {
        channel.writeAndFlush(message.payload);
        return;
      }
    }

    newChannelToSend(message);
  }

  private void newChannelToSend(RoutedMessage message) {
    ChannelFuture channelFuture = bootstrap.connect(message.destination);
    Channel channel = channelFuture.channel();
    channelFuture.addListener(
        (f) -> {
          if (f.isSuccess()) {
            channel.writeAndFlush(message.payload);
          } else {
            log.info(
                String.format(
                    "Cannot connect to %s because of: %s", message.destination, f.cause()));
          }
        });
    clientMap.put(message.destination, channel);
  }

  public void end() {
    isEnd = true;
    try {
      interrupt();
      join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void sendMessageTo(SocketAddress dst, Message msg) throws InterruptedException {
    messageQueue.put(new RoutedMessage(dst, msg));
  }
}
