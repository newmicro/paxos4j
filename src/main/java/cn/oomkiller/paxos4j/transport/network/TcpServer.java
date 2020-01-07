package cn.oomkiller.paxos4j.transport.network;

import cn.oomkiller.paxos4j.transport.codec.FrameDecoder;
import cn.oomkiller.paxos4j.transport.codec.ProtocolDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpServer extends Thread {
    private final String ip;
    private final int port;

    private final ServerBootstrap serverBootstrap = new ServerBootstrap();
    private MessageHandler handler;
    private boolean isEnd;

    public TcpServer(String ip, int port, int ioThreadCount) {
        this.ip = ip;
        this.port = port;
    }

    public void setMessageHandler(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final TcpServerHandler serverHandler = new TcpServerHandler(handler);
        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new FrameDecoder());
                            p.addLast(new ProtocolDecoder());
                            p.addLast(serverHandler);
                        }
                    });

            // Start the server.
            serverBootstrap.bind(ip, port).sync();

            // Wait until the server socket is closed.
            while(true) {
                if (isEnd) {
                    return;
                }

                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    // log.debug(String.format("Interrupted because of: %s"), e);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void end() {
        isEnd = true;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
